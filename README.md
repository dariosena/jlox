# Crafting Interpreters

## A Tree-Walker Interpreter

- Lox Reference
    - Java 21 implementation of the book
- Some differences
    - Use of records with interfaces instead of abstract class with inheritance

## Syntax Grammar

```
program     -> declaration* EOF ;

declaration -> varDecl
             | statement ;

varDecl     -> "var" IDENTIFIER ( "=" expression )? ";" ;

statement   -> exprStmt
             | printStmt
             | block ;

block       -> "{" declaration* "}" ;

exprStmt    -> expression ";" ;

printStmt   -> "print" expression ";" ;

expression  -> assignment ;

assignment  -> IDENTIFIER "=" assignment
             | equality ;

primary     -> "true" | "false" | "nil"
             | NUMBER | STRING
             | "(" expression ")"
             | IDENTIFIER ;
```
