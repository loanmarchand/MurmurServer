# MurmurServer
Prérequis:

Java JDK version 11 ou supérieure doit être installé sur le système.
Les fichiers d'application doivent être téléchargés et extraits dans un dossier de votre choix.
Étapes à suivre:

1. Lancer la classe NetChooser:
Ouvrez une invite de commande ou un terminal et naviguez vers le dossier où vous avez extrait les fichiers d'application.
Entrez la commande "java NetChooser" et appuyez sur Enter pour lancer la classe NetChooser.
Sélectionnez l'interface réseau que vous souhaitez utiliser.

2. Modifier le fichier hosts:
Ouvrez le fichier hosts situé dans le dossier "C:\Windows\System32\drivers\etc" (pour Windows) ou "/etc/" (pour Linux ou macOS).
Ajoutez les adresses IP et les noms de domaine des serveurs auxquels vous souhaitez vous connecter. Utilisez la syntaxe suivante: <adresse_IP> <nom_de_domaine>
Enregistrez le fichier hosts.

3. Ajouter des clés AES dans le fichier configRelay:
Lancez la classe AesKey en entrant la commande "java AesKey" dans l'invite de commande ou le terminal.
Copiez la clé générée par l'application.
Ouvrez le fichier configRelay situé dans le dossier "resources/".
Ajoutez une paire de domaines et de clés AES pour chaque serveur que vous avez ajouté dans le fichier hosts. Utilisez la syntaxe suivante: <nom_de_domaine>;<clé_AES>
Enregistrez le fichier configRelay.

4. Lancer l'application
Sur un périphérique, lancez la class MurmurRelay en entrant la commande "java MurmurRelay" dans l'invite de commande ou le terminal.
Lancez pour chaque server la class MurmurServer depuis un périphérique different en entrant la commande "java MurmurServer" dans l'invite de commande ou le terminal.
