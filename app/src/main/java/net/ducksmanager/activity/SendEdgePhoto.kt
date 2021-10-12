package net.ducksmanager.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.author_notations.*
import kotlinx.android.synthetic.main.release_notes.view.*
import kotlinx.android.synthetic.main.row_suggested_issue.view.*
import kotlinx.android.synthetic.main.score.view.*
import kotlinx.android.synthetic.main.send_edge_photo.view.*
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
import net.ducksmanager.whattheduck.databinding.SendEdgePhotoBinding
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.util.*

class SendEdgePhoto : AppCompatActivity(), Medals {
    private lateinit var binding: SendEdgePhotoBinding
    private lateinit var publicationCode: String
    private lateinit var issueNumber: String

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

    private fun getSvgContents(width: Int, height: Int, username: String) =
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
                <rect x="0.5" y="0.5" width="${width - 1}" height="${height - 1}" fill="none" stroke="black" stroke-width="1" class="border"></rect>
            </svg>
        """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        publicationCode = intent.getStringExtra("publicationCode")!!
        issueNumber = intent.getStringExtra("issueNumber")!!

        binding = SendEdgePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleStep(1)

        binding.title.text = String.format(
            resources.getString(R.string.send_edge_photo_title),
            publicationCode,
            issueNumber
        )

        WhatTheDuck.appDB!!.contributionTotalPointsDao().contributions.observe(this, { contributions ->
            val photographerContributions = contributions.find { it.contribution == "edge_photographer" }!!
            setMedalDrawable(photographerContributions, binding.medalCurrent)
            setMedalDrawable(photographerContributions, binding.medalTarget, true)

            val currentMedalLevel = getCurrentMedalLevel(photographerContributions)
            val currentMedalMin = MEDAL_LEVELS[R.id.medal_edge_photographer]?.get(currentMedalLevel) ?: 0
            val currentMedalMax = MEDAL_LEVELS[R.id.medal_edge_photographer]?.get(currentMedalLevel+1) ?: 0
            val currentMedalProgress = photographerContributions.totalPoints - currentMedalMin
            binding.medalProgress.min = currentMedalMin
            binding.medalProgress.max = currentMedalMax
            binding.medalProgress.progress = currentMedalProgress
            binding.medalIncentive.text = getString(R.string.medal_incentive_2, 2)
            binding.medalProgressWrapper.visibility = View.VISIBLE
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
                alert(WeakReference(this@SendEdgePhoto), "Invalid selection")
            }
            else {
                val width = bitmap.width
                val height = bitmap.height
                if (width > height) {
                    alert(WeakReference(this@SendEdgePhoto), "The width of your selection must be smaller than its height")
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
                            "The width of the edge must be between 5 and 50 millimeters"
                        )
                    }
                    height !in 100..350 -> {
                        alert(
                            WeakReference(this@SendEdgePhoto),
                            "The height of the edge must be between 100 and 350 millimeters"
                        )
                    }
                    else -> {
                        val username = WhatTheDuck.currentUser!!.username
                        val base64 = encodeImage(binding.cropImageView.croppedImage!!)
                        val country = publicationCode.split("/")[0]
                        val magazine = publicationCode.split("/")[1]

                        val svgContents = getSvgContents(width, height, username)
                        EdgeCreator.api.upload(EdgePhoto(country, magazine, issueNumber, base64))
                            .enqueue(object : DmServer.Callback<Void>(
                                EdgeCreator.EVENT_UPLOAD_PHOTO,
                                this@SendEdgePhoto,
                                false
                            ) {
                                override fun onSuccessfulResponse(response: Response<Void>) {
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
                                            alert(
                                                WeakReference(this@SendEdgePhoto),
                                                "Thank you for your contribution! If your photo is accepted, you will receive an e-mail when the corresponding edge is published."
                                            )
                                        }

                                        override fun onErrorResponse(response: Response<Void>?) {
                                            alert(
                                                WeakReference(this@SendEdgePhoto),
                                                "An error occurred when sending the edge"
                                            )
                                        }
                                    })
                                }

                                override fun onErrorResponse(response: Response<Void>?) {
                                    alert(
                                        WeakReference(this@SendEdgePhoto),
                                        "An error occurred when sending the edge"
                                    )
                                }
                            })
                    }
                }
            }
            catch(e: NumberFormatException) {
                alert(
                    WeakReference(this@SendEdgePhoto),
                    "Please enter valid numbers in the width and height fields!"
                )
            }
        }
    }
}
