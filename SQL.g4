grammar SQL;

start
  : (select | insert | update | delete) EOF
  ;

select
  : SELECT '*' FROM table_name WHERE conditions
  ;

insert
    : INSERT INTO table_name '(' column (',' column)* ')' VALUES '(' value (',' value)* ')'
    ;

update
    : UPDATE table_name SET assignments WHERE conditions
    ;

assignments
    : assignment (',' assignment)*
    ;

assignment
    : column '=' value
    ;

delete
    : DELETE FROM table_name (WHERE conditions)?
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


SELECT : S E L E C T;
FROM : F R O M;
WHERE : W H E R E;
INSERT : I N S E R T;
INTO : I N T O;
VALUES : V A L U E S;
UPDATE : U P D A T E;
SET : S E T;
DELETE : D E L E T E;
AND : A N D;
OR : O R;
XOR : X O R;


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