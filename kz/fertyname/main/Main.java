package kz.fertyname.main;

import kz.fertyname.util.Cryptography;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Сообщение для шифрование: ");
        Scanner scanner = new Scanner(System.in);

        String inputPath = "input.jpg";
        String outputPath = "output.jpg";
        String message = Cryptography.dataEncrypto(scanner.next());

        System.out.println(message+" <- encoded");
        Cryptography.encode(inputPath, outputPath, message);


        System.out.println(Cryptography.dataDecrypt(Cryptography.decode(outputPath)) + " <- decoded");
    }


}
