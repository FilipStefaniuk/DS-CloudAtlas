1. Wyjaśnić niespójne zachowanie w testach:

	"SELECT min(sum(distinct(2 * level)) + 38 * size(contacts)) AS sth WHERE num_cores < 8"

ma zwracać(Z NULL wartością):

	/uw: sth: 80
	/pjwstk: sth: 80
	/: sth: NULL" 

a inne zapytania np. :
	
	"SELECT count(num_cores - size(some_names)) AS sth"

bez:
	/uw: sth: 2


2. Zmienić ajax na websockety ??
3. Serializacja przez Kryo a nie default
4. Poprawić klienta
5. Poprawić fetcher (sprawdzić czy działa dobrze na users)
6. Przetestować na students
7. Obsługa błędów i testy ???
