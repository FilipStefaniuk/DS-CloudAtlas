
Filip Stefaniuk - Zadanie zaliczeniowe 2

###################################################################################################

Instalacja:

	$ mvn install 

Uruchamianie:
Aby wsztstkie poniższe programy działały poprawnie należy najpierw uruchomić rmi:
    $ ./registry.sh
następnie można wygenerować parę kluczy dla querySignera:
    $ ./gen_keystore <keystore_pass> <key_pass>
    potem należy skopiować powstały keystore do resources agenta i querySignera, oraz
    uzupełnić pliki .properties o odpowiednie hasła.

Żeby obserwować działanie programu można zmieniać poziom z jakiem (i z których klas) jest loggowanie,
na poziomie DEBUG każdy handler wypisuje informację jaka wiadomość do niego przyszła i jaką gdzie wysłał.

!!! Po każdej zmianie w konfiguracjach należy na nowo zbudować program tak żeby zmiany zostały zaimportowane
do odpowiednich plików .jar z których wykonywany jest program.
	
*Interpreter:
	$ ./interpreter.sh

*QuerySigner:
    $ ./query_signer.sh

*Agent (Uruchamia też powiązanego z nim Fetchera):
    $ ./agent.sh <file.properties>

    Za pomocą argumentu można sterować z którym plikiem properties z resources uruchomić agenta,
    dzięki temu można w łatwy sposób uruchomić kilu na jednej maszynie należy jedynie pamiętać aby
    kolejne programy miały różne ID i działały na różnych portach.

*WebClient (Embedded Tomcat server):
    $ ./client.sh

###################################################################################################

Brakujące rzeczy:

*Fetcher:
	- brak atrybutu DNS names

*Model:
	- Serializaja nie jest przez Kryo

	Przy obecnej architekturze systemu jest bardzo ciężko dodać serializację przez kryo, jest to spowodowane faktem,
	że kryo wymaga aby każda klasa miała publiczny bezargumentowy konstruktor. W obecnej wersji programu cała komunikacja
	jest oparta na generycznych wiadomościach które przesyłają klasy z modułu "model". Wiele z tych klas nie pozwala na
	tworzenie ich bez argumentów, mają pola "final" a dodanie tam tych konstruktorów mogło by sporo zepsuć i wymagałoby
	poważnych zmian w module. Innym rozwiązaniem jest zmiana klas wiadomomości którymi moduły się komunikują. To niestety
	wymagałoby wielu zmian w module agent.

*Client:
	- Brak frontendu do setContacts
    - Brak wykresu

    Przepisałem klienta tak żeby działał przy wykożystaniu Spring Boot, dzięki temu nie trzeba importować
    spakowanego pliku .war do serwera, wszystko jest w wykonywalnym pliku .jar Klient może teraz też komunikować
    się z różnymi agentami - robi to poprzez sprawdzanie rmi registry.

*Agent:
    - Brak usuwania starych ZMI
    - Brak możliwości wyboru strategii dla Gossip


