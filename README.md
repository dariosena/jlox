# Crafting Interpreters

## A Tree-Walker Interpreter

- Lox Reference
    - Java 21 implementation of the book
- Some differences
    - Use of records with interfaces instead of abstract class with inheritance

## Lexical Grammar

```
NUMBER      -> DIGIT+ ( "." DIGIT+ )? ;
STRING      -> "\"" <any char except "\"">* "\"" ;
IDENTIFIER  -> ALPHA ( ALPHA | DIGIT )* ;
ALPHA       -> "a" ... "z" | "A" ... "Z" | "_" ; 
DIGIT       -> "0" ... "9" ;
```

## Syntax Grammar

### Statements

```
program     -> declaration* EOF ;

declaration -> varDecl
             | statement ;

varDecl     -> "var" IDENTIFIER ( "=" expression )? ";" ;

statement   -> exprStmt
             | isStmt
             | printStmt
             | block ;

ifStmt      -> "if" "(" expression ")" statement
             | ("else" statement)? ;

block       -> "{" declaration* "}" ;

exprStmt    -> expression ";" ;

printStmt   -> "print" expression ";" ;
```

### Expressions

```
expression  -> assignment ;

assignment  -> IDENTIFIER "=" assignment
             | logic_or ;

logic_or    -> logic_and ( "or" logic_and )* ;
logic_and   -> equality ( "and" equality )* ;
equality    -> comparison ( ("!=" | "==") comparison )* ;
comparison  -> term ( ( ">" | ">=" | "<" | "<=") term )* ;
term        -> factor ( ( "-" | "+" ) factor)* ;
factor      -> unary ( ( "/" | "*" ) unary)* ;

unary       -> ( "!" | "-") unary | call ;
call        -> TBD
primary     -> "true" | "false" | "nil"
             | NUMBER | STRING
             | "(" expression ")"
             | IDENTIFIER ;
```

### Utility Rules

```
TBD
```
