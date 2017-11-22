
Filip Stefaniuk - Zadanie zaliczeniowe 1

###################################################################################################

Instalacja:

	$ mvn install 

Uruchamianie:
	
*Interpreter:
	
	$ ./interpreter.sh

*Reszta:

	$ ./startup.sh - uruchamia rmiregistry agenta i fetcher
	
	rmiregistry jest uruchamiane na porcie 1324, nie domyślnym.

	Klient spakowany jest w plik .war i można go zainstalować na serwerze.
	W szczególności: "$ mvn tomcat7:deploy" wywołane w katalogu client, zainstaluje
	aplikację na serwerze tomcat. Przy założeniach że:
		- Server tomcat działa na porcie 8081
		- Jest na nim stworzony użytkownik "admin" z hasłem "password" i rolą "manager-script"


###################################################################################################

Brakujące rzeczy:

*Fetcher:
	- brak czytania konfiguracji z pliku
	- brak atrybutu DNS names

*Model:
	- Serializaja nie jest przez Kryo

*Client:
	- Brak frontendu do setContacts
