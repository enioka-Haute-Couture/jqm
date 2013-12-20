# JQM

## Présentation

JQM (Job Queue Management) est un gestionnaire de batch sous licence Apache qui permet de traiter sur des noeuds
de traitement répartis toutes les tâches potentiellement longues qui ne sont pas désirables dans un serveur d'application.
Ce logiciel s'adresse à toute application qui souhaite gérer l'exécution de ses tâches hors du serveur d'application.

Une tâche peut être déclenchée depuis un appel Web Service ou une API par une application web, un ordonnanceur ou un flux d'interface.

L'outil propose de nombreuses fonctionnalités comme l’arrêt d’une tâche, la récupération de fichiers générés, la priorisation d’une tâche et bien d’autres.

JQM a été développé en Java SE 1.6, utilise Hibernate/JPA 2.0 comme ORM, et une base de donnée comme référentiel de configuration et file d'attente des traitements.
JQM est compatible avec les bases HSQL, MySQL et Oracle, les serveurs d’application WebSphere et Glassfish (prochainement Tomcat) pour l'API cliente et gère les ressources JNDI pour les bases de données et les brokers de messages.
L'outil est compatible avec les projets Maven et tout particulièrement la gestion des dépendances et des projets parents.


## Fonctionnement
### Architecture

JQM est composé de trois grandes parties :
        - les moteurs de traitements (des JVM standalone) qui exécutent les tâches. Il est possible de déployer plusieurs moteurs (ou noeuds) de traitements pour des raisons de perforomance ou de haute disponibilité
        - une base de données qui joue le rôle de file de traitement et de référentiel de configuration
        - les clients (une application Web dans un serveur d'application, une ligne de commande, un ordonnaceur, un autre job JQM etc.) qui soumettent des jobs à JQM

Les noeuds de traitement sont reliés à des files de traitement en base de données et ont chacun un intervalle de polling et un nombre défini de jobs pouvant tourner simultanément.

Par exemple:
	- VIPqueue = 10 jobs simultanées + intervalle de fréquence de 1 seconde
	- SLOWqueue = 3 jobs en simultanés + intervalle de fréquence de 15 min

### Cycle de vie d'une tache

Le cycle de vie d'un job passe par quatre états différents .

Après avoir été ajoutée à la file d'attente, la tâche prend le status "SUBMITTED". Une fois que le job est ¨attrapé¨ par un noeud,
son statut passe à l'état "ATTRIBUTED" suivi de ¨RUNNING¨ une fois que l'exécution de celui-ci a commencé.
Le job à la fin de son execution a deux états possibles, "CRASHED" si le job'a pas fonctionné correctement ou "ENDED" si tout le
processus s'est déroulé correctement.

Toutes les informations de suivi d'un job sont logguées dans la base de données de JQM  : état, progression, logs, temps d'exécution... Ceci permet aussi bien
de produire des statistiques sur les performances des jobs que de réaliser un suivi avec des outils de supervision.

### Fonctionnalités

#### Pour les développeurs

- Pour les développeurs de traitement
Un job est défini comme tel une fois qu'il "extends" de la classe _JobBase_ de l'API _jqm-api_.
Au sein d'un job, il est possible de récupérrer des ressources via JNDI (base de données, broker de message, répertoire de traitement...), d'envoyer une progression,
un message de log ou de mettre en file un autre job.

- Pour les clients des traitements
Il existe plusieurs moyens d'utiliser JQM.
	- Par le biais de l'API _jqm-clientapi_ qui permet d'avoir toutes les fonctionnalités existantes, à savoir
la possibilité de mettre un job en file, de regarder son état, de l'annuler, de le changer de file et bien d'autres.
	- Par le biais d'un web service
	- Par une interface en ligne de commande
	- Par une IHM web (très frustre à l'heure actuelle)

#### Pour les administrateurs

Un administrateur à la possibilité de consulter les logs, de gérer les connexions JNDI, de paramétrer les associations entre les jobs et  les files.

### Exemple de job

public class Caller extends JobBase
{
	@Override
	public void start()
	{
		Map<String, String> params = new HashMap<String, String>();
		p.put(“myParam1”, “Garry”);
		p.put(“myParam2”, “Bob”);

		// enQueue is a JobBase method.
		// It is used to enqueue a Job, here “Called”.
		enQueue(“CalledJob”, “Bob”, null, null, null, null, null,
                    null, null, null, null, params);
	}
}

## Origine du projet

Le projet a été développé par l'entreprise *enioka* dans le cadre d'un projet pour l'un de ses clients pour l'intégration d'un ERP.
Suite à la réalisation de ce projet, il a été convenu que JQM deviendrait open source afin de combler le manque actuel de ce type d'outils libres dans un contexte java.
Le code source et la documentation sont disponilbes sur github
