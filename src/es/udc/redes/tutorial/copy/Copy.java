package es.udc.redes.tutorial.copy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Copy {
    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("Format: copy <from_file> <to_file>");
        }
        else if (args[0].endsWith(".txt") || args[0].endsWith(".TXT")){
            CopyText.copy(args[0], args[1]);
        }
        else{
            CopyBinary.copy(args[0], args[1]);
        }
    }
}

class CopyText {
    static void copy(String from_file, String to_file){
        FileReader input = null;
        FileWriter output = null;
        
        try{
            input = new FileReader(from_file);
            output = new FileWriter(to_file);
            
            int c;
            while((c = input.read()) != -1){
                output.write(c);
            }
        }
        catch (IOException e){
            System.out.println("I/O Error " + e.getMessage());
        }
        finally{
            try{
                if (input != null){
                    input.close();
                }
                if (output != null){
                    output.close();
                }
            }
            catch (IOException e){
                System.out.println("I/O Exception " + e.getMessage());
            }
        }
    }
}

class CopyBinary{
    static void copy(String from_file, String to_file){
        FileInputStream input = null;
        FileOutputStream output = null;
        
        try{
            input = new FileInputStream(from_file);
            output = new FileOutputStream(to_file);
            
            int c;
            while((c = input.read()) != -1){
                output.write(c);
            }
        }
        catch (IOException e){
            System.out.println("I/O Error " + e.getMessage());
        }
        catch (Exception e){
            System.out.println("Error " + e.getMessage());
        }
        finally{
            try{
                if (input != null){
                    input.close();
                }
                if (output != null){
                    output.close();
                }
            }
            catch (IOException e){
                System.out.println("I/O Exception " + e.getMessage());
            }
        }
    }
}