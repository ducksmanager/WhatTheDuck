<?php
error_reporting(E_ALL);
$database=isset($_GET['coa']) ? 'coa' : 'db301759616';

@include_once('Inducks.class.php');
@include_once('../Inducks.class.php');
include_once ('auth.php');

$version=isset($_GET['version']) ? $_GET['version'] : '1.0';


if (isset($_GET['storycode'])) {
	$final=new stdClass();
	
	$code=mysql_real_escape_string($_GET['storycode']);
	$requete='SELECT inducks_issue.publicationcode AS publicationcode, inducks_issue.issuenumber AS issuenumber, inducks_publication.title AS title '
			.'FROM inducks_issue '
			.'INNER JOIN inducks_entry ON inducks_issue.issuecode = inducks_entry.issuecode '
			.'INNER JOIN inducks_storyversion ON inducks_entry.storyversioncode = inducks_storyversion.storyversioncode '
			.'INNER JOIN inducks_publication ON inducks_issue.publicationcode = inducks_publication.publicationcode '
			.'WHERE storycode = \''.$code.'\' '
			.'ORDER BY publicationcode, issuenumber';
	$resultats_tab=array();
	$resultats=mysql_query($requete);
	
	$noms_magazines=array();
	$numeros=array();
	while($resultat = mysql_fetch_array($resultats)) {
		$numero=new stdClass();
		$numero->magazine=$resultat['publicationcode'];
		$numero->numero=$resultat['issuenumber'];
		$titre_magazine=$resultat['title'];
				
		$numeros[]=$numero;
		
		if (!in_array($numero->magazine,$noms_magazines)) {
			$nom_magazine=new stdClass();
			$nom_magazine->nom_abrege=$numero->magazine;
			$nom_magazine->nom_complet=$titre_magazine;
			$noms_magazines[]=$nom_magazine;
		}
	}
	
	$final->numeros=$numeros;
	$final->static=$noms_magazines;
	
	echo json_encode($final);
}
else if (isset($_GET['pseudo_user']) && isset($_GET['mdp_user'])) {
	if (isset($_GET['coa'])) {
		DatabasePriv::connect('coa');
		mysql_query('SET NAMES UTF8');
		$retour=new stdClass();
		$retour->static=new stdClass();
		
		if (isset($_GET['liste_pays'])) {
			$liste_pays=array();
			$requete_liste_pays='SELECT countrycode, countryname '
							   .'FROM inducks_countryname '
							   .'WHERE languagecode=\'fr\' and countryname <>\'fake\' '
							   .'ORDER BY countryname';
			if (isset($_GET['debug']))
				echo $requete_liste_pays;
			$resultats_liste_pays=mysql_query($requete_liste_pays);
			while($pays = mysql_fetch_array($resultats_liste_pays)) {
				$liste_pays[$pays['countrycode']]=$pays['countryname'];
			}
			$retour->static->pays=$liste_pays;
		}
		else if (isset($_GET['liste_magazines']) && isset($_GET['pays'])) {
			$liste_magazines=array();
			$requete_liste_magazines='SELECT publicationcode, title '
								    .'FROM inducks_publication '
								    .'WHERE countrycode=\''.$_GET['pays'].'\' '
								    .'ORDER BY publicationcode';
			if (isset($_GET['debug']))
				echo $requete_liste_magazines;
			$resultats_liste_magazines=mysql_query($requete_liste_magazines);
			while($magazine = mysql_fetch_array($resultats_liste_magazines)) {
				$liste_magazines[$magazine['publicationcode']]=$magazine['title'];
			}
			$retour->static->magazines=$liste_magazines;
		}
		else if (isset($_GET['liste_numeros']) && isset($_GET['magazine'])) {
			$liste_numeros=array();
			$requete_liste_numeros='SELECT issuenumber '
								  .'FROM inducks_issue '
								  .'WHERE publicationcode=\''.$_GET['magazine'].'\'';
			if (isset($_GET['debug']))
				echo $requete_liste_numeros;
			$resultats_liste_numeros=mysql_query($requete_liste_numeros);
			while($numero = mysql_fetch_array($resultats_liste_numeros)) {
				if ($version=='1.1') {
					$liste_numeros[]=Inducks::nettoyer_numero($numero['issuenumber']);
				}
				else
					$liste_numeros[]=$numero['issuenumber'];
			}
			$retour->static->numeros=$liste_numeros;
		}
		echo json_encode($retour);
	}
	else { 
		Inducks::$use_local_db=false;
		// Récupération des informations sur la collection de l'utilisateur
		$pseudo=mysql_real_escape_string($_GET['pseudo_user']);
		$mdp=mysql_real_escape_string($_GET['mdp_user']);
			
		$requete='SELECT ID FROM users WHERE username=\''.$pseudo.'\' AND password=\''.$mdp.'\'';
		$resultats=Inducks::requete_select($requete,'db301759616','ducksmanager.net');
		
		if (isset($_GET['debug']))
			echo $requete.'<br />';
		//$resultats=mysql_query($requete);
		
		$action=isset($_GET['action']) ? $_GET['action'] : '';
		switch($action) {
			case 'signup':
				$user= $_GET['pseudo_user' ];
				$pass= $_GET['mdp_user' ];
				$pass2=$_GET['mdp_user2'];
				$email=$_GET['email'];
				
				include_once('Affichage.class.php');
				
				$erreur = Affichage::valider_formulaire_inscription($user, $pass, $pass2);
				
				if (is_null($erreur)) {
					DM_Core::$d->nouveau_user($user, $email,$pass);
					echo 'OK';
				}
				else {
					echo utf8_encode(html_entity_decode($erreur));
				}
			break;
			default:		
				if(count($resultats) > 0) {
					$id_utilisateur=$resultats[0]['ID'];
					if (isset($_GET['ajouter_numero'])) {
						list($pays,$magazine)=explode('/',$_GET['pays_magazine']);
						$numero=$_GET['numero'];
						$etat=$_GET['etat'];
						
						if ($version == '1.0') {
							$requete='INSERT INTO numeros(Pays,Magazine,Numero, Etat, ID_Acquisition, ID_Utilisateur) '
									.'VALUES(\''.$pays.'\', \''.$magazine.'\', \''.$numero.'\', \'indefini\', -2, '.$id_utilisateur.')';
						}
						else {
							$requete='INSERT INTO numeros(Pays,Magazine,Numero, Etat, ID_Acquisition, ID_Utilisateur) '
									.'VALUES(\''.$pays.'\', \''.$magazine.'\', \''.$numero.'\', \''.$etat.'\', -2, '.$id_utilisateur.')';
						}
						$resultats=Inducks::requete_select($requete,'db301759616','ducksmanager.net');
						
						if (isset($_GET['debug']))
							echo $requete.'<br />';
						if (count($resultats)==0 && $resultats === array())
							echo 'OK';
						else
							print_r($resultats);
					}
					else {
						DatabasePriv::connect('coa');
						mysql_query('SET NAMES UTF8');
						foreach($resultats as $resultat) {
							$retour=new stdClass();
							$numeros=array();
							$pays=array();
							$magazines=array();
							$requete_numeros='SELECT * FROM numeros WHERE ID_Utilisateur='.$resultat['ID'].' ORDER BY Pays, Magazine, Numero';
							$resultats_numeros=Inducks::requete_select($requete_numeros,'db301759616','ducksmanager.net');
							foreach($resultats_numeros as $resultat_numero) {
								$pays_magazine=$resultat_numero['Pays'].'/'.$resultat_numero['Magazine'];
								$numero=$resultat_numero['Numero'];
								$etat=$resultat_numero['Etat'];
								
								if (!array_key_exists($pays_magazine,$numeros)) {
									$numeros[$pays_magazine]=array();
									$magazines[$pays_magazine]=$pays_magazine;
								}
								switch($version) {
									case '1.0':
										$numeros[$pays_magazine][]=$numero;								
									break;
									default:
										$numero_et_etat=new stdClass();
										$numero_et_etat->Numero=$numero;
										$numero_et_etat->Etat=$etat;
										$numeros[$pays_magazine][]=$numero_et_etat;									
									break;
								}
									
							}
							
							foreach(array_keys($magazines) as $nom_abrege) {
								$requete_nom_complet_magazine='SELECT inducks_countryname.countryname as countryname, inducks_publication.title as title '
															 .'FROM inducks_publication '
															 .'INNER JOIN inducks_countryname ON inducks_publication.countrycode = inducks_countryname.countrycode '
															 .'WHERE inducks_countryname.languagecode=\'fr\' '
															 .'  AND inducks_publication.publicationcode=\''.$nom_abrege.'\'';
								$resultats_nom_complet_magazine=mysql_query($requete_nom_complet_magazine);
								while($resultat_nom_magazine = mysql_fetch_array($resultats_nom_complet_magazine)) {					
									list($nom_pays,$nom_magazine)=explode('/',$nom_abrege);
									$pays[$nom_pays]=$resultat_nom_magazine['countryname'];
									$magazines[$nom_abrege]=$resultat_nom_magazine['title'];
								}
							}
						}
						
						$retour->numeros = $numeros;
						$retour->static=new stdClass();
						$retour->static->magazines=$magazines;
						$retour->static->pays=$pays;
						echo json_encode($retour);
						
					}
				}
				else
					echo '0';
			break;
		}
	}
}
