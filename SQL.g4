grammar SQL;

start
  : (select | insert | update | delete | create_table | create_index) EOF
  ;

select
  : SELECT STAR FROM table_name WHERE conditions
  ;

insert
    : INSERT INTO table_name OPEN_PAREN column (COMMA column)* CLOSE_PAREN VALUES OPEN_PAREN value (COMMA value)* CLOSE_PAREN
    ;

update
    : UPDATE table_name SET assignments WHERE clustering_key '=' clustering_key_value
    ;

create_table
    : CREATE TABLE table_name OPEN_PAREN create_table_column (COMMA create_table_column)* (COMMA PRIMARY_KEY OPEN_PAREN clustering_key CLOSE_PAREN )? CLOSE_PAREN
    ;

create_index
    : CREATE INDEX IDENTIFIER ON table_name OPEN_PAREN column (COMMA column)* CLOSE_PAREN
    ;

create_table_column
    : column type (PRIMARY_KEY)?
    ;

type
    : INT_TYPE | FLOAT_TYPE | STRING_TYPE | DATE_TYPE | DOUBLE_TYPE | TEXT_TYPE | VARCHAR(OPEN_PAREN INT CLOSE_PAREN)?
    ;

clustering_key
    : column
    ;

clustering_key_value
    : value
    ;

assignments
    : assignment (COMMA assignment)*
    ;

assignment
    : column EQ value
    ;

delete
    : DELETE FROM table_name (WHERE assignments)?
    ;

column
  : IDENTIFIER
  ;

table_name
  : IDENTIFIER
  ;

conditions
    : condition (compound condition)*
    ;

compound
    : AND | OR | XOR
    ;

condition
  : column operator value
  ;

operator
  : EQ | LT | LE | GT | GE
  ;

value
  : DATE | STRING | INT | FLOAT
  ;


SELECT : ' '* S E L E C T ' '*;
FROM : ' '* F R O M' '*;
WHERE : ' '* W H E R E' '*;
INSERT : ' '* I N S E R T' '*;
INTO : ' '* I N T O' '*;
VALUES : ' '* V A L U E S' '*;
UPDATE : ' '* U P D A T E' '*;
SET : ' '* S E T' '*;
DELETE : ' '* D E L E T E' '*;
AND : ' '* A N D' '*;
OR : ' '* O R' '*;
XOR : ' '* X O R' '*;
INDEX : ' '* I N D E X' '*;
TABLE : ' '* T A B L E' '*;
CREATE : ' '* C R E A T E' '*;
PRIMARY_KEY : ' '* P R I M A R Y ' ' K E Y' '*;
DATE_TYPE: ' '* D A T E' '*;
INT_TYPE: ' '* I N T' '*;
FLOAT_TYPE: ' '* F L O A T' '*;
STRING_TYPE: ' '* S T R I N G' '*;
DOUBLE_TYPE: ' '* D O U B L E' '*;
ON : ' '* O N' '*;
VARCHAR : ' '* V A R C H A R' '*;
TEXT_TYPE : ' '* T E X T '_' T Y P E' '*;
COMMA : ' '* ',' ' '*;
OPEN_PAREN : ' '* '(' ' '*;
CLOSE_PAREN : ' '* ')' ' '*;
STAR : ' '* '*' ' '*;

EQ : '=';
LT : '<';
GT : '>';
LE : '<=';
GE : '>=';


DATE
    : '\'' [0-9]+ '-' [0-9]+ '-' [0-9]+ '\'' { setText(getText().substring(1, getText().length()-1).replaceAll("-", "/")); }
    ;

STRING :
    '\'' ( ~'\'' | '\'\'' )* '\'' { setText(getText().substring(1, getText().length()-1)); };

INT
  : [0-9]+
  ;

FLOAT
  : [0-9]+ '.' [0-9]+
  ;


IDENTIFIER :
    [a-zA-Z_] [a-zA-Z_0-9]* ;


fragment DIGIT : [0-9] ;
fragment A : [aA] ;
fragment B : [bB] ;
fragment C : [cC] ;
fragment D : [dD] ;
fragment E : [eE] ;
fragment F : [fF] ;
fragment G : [gG] ;
fragment H : [hH] ;
fragment I : [iI] ;
fragment J : [jJ] ;
fragment K : [kK] ;
fragment L : [lL] ;
fragment M : [mM] ;
fragment N : [nN] ;
fragment O : [oO] ;
fragment P : [pP] ;
fragment Q : [qQ] ;
fragment R : [rR] ;
fragment S : [sS] ;
fragment T : [tT] ;
fragment U : [uU] ;
fragment V : [vV] ;
fragment W : [wW] ;
fragment X : [xX] ;
fragment Y : [yY] ;
fragment Z : [zZ] ;

WS: [ \n\t\r]+ -> skip;