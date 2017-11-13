Niespójne zachowanie w testach:

	"SELECT min(sum(distinct(2 * level)) + 38 * size(contacts)) AS sth WHERE num_cores < 8"

ma zwracać(Z NULL wartością):

	/uw: sth: 80
	/pjwstk: sth: 80
	/: sth: NULL" 

a inne zapytania np. :
	
	"SELECT count(num_cores - size(some_names)) AS sth"

bez:
	/uw: sth: 2
