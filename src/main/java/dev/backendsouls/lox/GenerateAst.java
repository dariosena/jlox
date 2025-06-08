package dev.backendsouls.lox;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        var outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(final String outputDir, final String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package dev.backendsouls.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public interface " + baseName + " {");

        // The AST classes.
        for (String type : types) {
            String recordName = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, recordName, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(
            final PrintWriter writer, final String baseName,
            final String recordName, final String fieldList
    ) {
        var record = "    record " + recordName + "(" + fieldList + ")" + " implements " + baseName + " {";

        writer.println(record);
        writer.println("    }");
        writer.println();
    }
}
