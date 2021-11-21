package net.ducksmanager.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import net.ducksmanager.api.DmServer
import net.ducksmanager.api.EdgeCreator
import net.ducksmanager.persistence.models.composite.*
import net.ducksmanager.persistence.models.edgecreator.EdgeCanvas
import net.ducksmanager.persistence.models.edgecreator.EdgePhoto
import net.ducksmanager.util.Medals
import net.ducksmanager.util.Medals.Companion.MEDAL_LEVELS
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.alert
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.info
import net.ducksmanager.whattheduck.databinding.SendEdgePhotoBinding
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max

class SendEdgePhoto : AppCompatActivity(), Medals {
    private lateinit var binding: SendEdgePhotoBinding
    private lateinit var publicationCode: String
    private lateinit var issueNumber: String
    private var popularity: Int = 1

    companion object {
        const val MY_PERMISSIONS_REQUEST = 0
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return "data:image/jpeg;base64,${Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)}"
    }

    private fun toggleStep(step: Int) {
        binding.form1.visibility = if (step == 1) { View.VISIBLE } else { View.GONE }
        binding.form2.visibility = if (step == 2) { View.VISIBLE } else { View.GONE }
        binding.form3.visibility = if (step == 3) { View.VISIBLE } else { View.GONE }
        binding.cameraWrapper.visibility = if (step == 1) { View.VISIBLE } else { View.GONE }
        binding.cropImageView.visibility = if (step == 2) { View.VISIBLE } else { View.GONE }
        binding.croppedImage.visibility = if (step == 3) { View.VISIBLE } else { View.GONE }
        binding.dimensionsWrapper.visibility = if (step == 3) { View.VISIBLE } else { View.GONE }
    }

    private fun getSvgContents(width: Int, height: Int, username: String, photoFileName: String) =
        """
            <svg
                id="edge-canvas"
                viewBox="0 0 $width $height"
                width="${width * 1.5}"
                height="${height * 1.5}"
                xmlns="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                preserveAspectRatio="none"
                class="edge-canvas position-relative">
                <metadata type="contributor-photographer">$username</metadata>
                <metadata type="photo">$photoFileName</metadata>
                <rect x="0.5" y="0.5" width="${width - 1}" height="${height - 1}" fill="none" stroke="black" stroke-width="1" class="border"></rect>
            </svg>
        """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST)
        }
        else {
            show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != MY_PERMISSIONS_REQUEST) {
            finishActivity(requestCode)
            return
        }
    }

    fun show() {
        publicationCode = intent.getStringExtra("publicationCode")!!
        issueNumber = intent.getStringExtra("issueNumber")!!
        popularity = intent.getIntExtra("popularity", 1)

        binding = SendEdgePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleStep(1)

        val publicationNames = WhatTheDuck.appDB!!.inducksPublicationDao().findByPublicationCodes(
            setOf(publicationCode))

        binding.title.text = String.format(
            resources.getString(R.string.send_edge_photo_title),
            publicationNames.find { it.publicationCode == publicationCode }!!.title,
            issueNumber
        )

        WhatTheDuck.appDB!!.contributionTotalPointsDao().contributions.observe(this, { contributions ->
            val photographerContributions = contributions.find { it.contribution == "edge_photographer" }!!
            setMedalDrawable(photographerContributions, binding.medalCurrent)
            setMedalDrawable(photographerContributions, binding.medalTarget, true)

            val currentMedalLevel = getCurrentMedalLevel(photographerContributions)
            if (binding.medalProgress.javaClass.kotlin.members.any { it.name == "min" }) {
                binding.medalProgress.min = MEDAL_LEVELS["edge_photographer"]!![currentMedalLevel] ?: 0
            }
            binding.medalProgress.max = MEDAL_LEVELS["edge_photographer"]!![currentMedalLevel+1] ?: 0
            binding.medalProgress.progress = photographerContributions.totalPoints
            binding.medalIncentive.text = getString(R.string.medal_incentive_2, popularity)
            binding.medalProgressWrapper.visibility = View.VISIBLE
            val animator = ObjectAnimator.ofInt(binding.medalProgress, "progress",  max(photographerContributions.totalPoints + popularity, 2))
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.setDuration(2000).start()
        })

        binding.takePhoto.setOnClickListener {
            binding.camera.takePicture()
        }

        binding.camera.setLifecycleOwner(this)
        binding.camera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                result.toBitmap { bitmap: Bitmap? ->
                    if (bitmap != null) {
                        toggleStep(2)
                        binding.cropImageView.setImageBitmap(bitmap)

                    }
                }
                super.onPictureTaken(result)
            }
        })

        binding.resetPhoto.setOnClickListener {
            toggleStep(1)
        }

        binding.cropImageOK.setOnClickListener {
            val bitmap = binding.cropImageView.croppedImage
            if (bitmap == null) {
                alert(WeakReference(this@SendEdgePhoto), R.string.invalid_selection)
            }
            else {
                val width = bitmap.width
                val height = bitmap.height
                if (width > height) {
                    alert(WeakReference(this@SendEdgePhoto), R.string.error_width_is_bigger_than_height)
                }
                else {
                    binding.croppedImage.setImageBitmap(bitmap)
                    toggleStep(3)
                }
            }
        }

        binding.backToCropping.setOnClickListener {
            toggleStep(2)
        }

        binding.sendEdgeImage.setOnClickListener {
            try {
                val width = binding.width.text.toString().toInt()
                val height = binding.height.text.toString().toInt()
                when {
                    width !in 5..50 -> {
                        alert(
                            WeakReference(this@SendEdgePhoto),
                            R.string.error_width_restrictions
                        )
                    }
                    height !in 100..350 -> {
                        alert(
                            WeakReference(this@SendEdgePhoto),
                            R.string.error_height_restrictions
                        )
                    }
                    else -> {
                        val username = WhatTheDuck.currentUser!!.username
                        val base64 = encodeImage(binding.cropImageView.croppedImage!!)
                        val country = publicationCode.split("/")[0]
                        val magazine = publicationCode.split("/")[1]

                        EdgeCreator.api.upload(EdgePhoto(country, magazine, issueNumber, base64))
                            .enqueue(object : DmServer.Callback<HashMap<String, String>>(
                                EdgeCreator.EVENT_UPLOAD_PHOTO,
                                this@SendEdgePhoto,
                                false
                            ) {
                                override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                                    val svgContents = getSvgContents(width, height, username, response.body()!!["fileName"]!!)
                                    EdgeCreator.api.saveEdgeCanvas(
                                        EdgeCanvas(
                                            country = country,
                                            magazine = magazine,
                                            issuenumber = issueNumber,
                                            contributors = hashMapOf("photographers" to hashMapOf("username" to username)),
                                            content = svgContents
                                        )
                                    ).enqueue(object : DmServer.Callback<Void>(
                                        EdgeCreator.EVENT_SAVE_EDGE_MODEL,
                                        this@SendEdgePhoto,
                                        false
                                    ) {
                                        override fun onSuccessfulResponse(response: Response<Void>) {
                                            info(
                                                WeakReference(this@SendEdgePhoto),
                                                R.string.send_edge_thanks,
                                                Toast.LENGTH_LONG
                                            )
                                            startActivity(Intent(this@SendEdgePhoto, IssueList::class.java))
                                        }

                                        override fun onFailureFailover() { onEdgeSubmitError() }

                                        override fun onErrorResponse(response: Response<Void>?) { onEdgeSubmitError() }
                                    })
                                }

                                override fun onErrorResponse(response: Response<HashMap<String, String>>?) { onEdgeSubmitError() }

                                override fun onFailureFailover() { onEdgeSubmitError() }

                                private fun onEdgeSubmitError() {
                                    alert(
                                        WeakReference(this@SendEdgePhoto),
                                        R.string.error_sending_edge
                                    )
                                }
                            })
                    }
                }
            }
            catch(e: NumberFormatException) {
                alert(
                    WeakReference(this@SendEdgePhoto),
                    R.string.error_invalid_number_format
                )
            }
        }
    }
}
