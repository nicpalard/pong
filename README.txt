Le Pong est déjà compilé sous forme de .jar.
Pour le lancer il suffit de taper la commande :
     	java -jar PongFinal.jar nbPlayer ip1 ip2 ip3

Si vous êtes l'initiateur de la partie, vous devez donc seulement préciser le
nombre de joueurs.
Si vous êtes le joueur 2/3/4 vous préciser le nombre de joueur et l'IP des joueurs déjà connectés.

Exemple :
Moi joueur 3 souhaite me connecter a une partie 4 joueurs.
Je me connecte au J1 dont l'IP est : 10.11.12.13
et au J2 dont l'IP est : 10.11.12.14, je tape donc la commande suivante :
   java -jar PongFinal.jar 4 10.11.12.13 10.11.12.14


Vous pouvez aussi compiler le projet a partir des sources en l'important sous
Eclipse ou Intellij.
