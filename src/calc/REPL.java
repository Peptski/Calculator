package calc;

import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

class REPL {

    public static void main(String[] args) {
        new REPL().program();
    }

    final Scanner scan = new Scanner(in);
    final Calculator calculator = new Calculator();

    void program() {

        while (true) {
            out.print("> ");
            String input = scan.nextLine();
            if (input.toLowerCase().equals("quit")){
                break;
            }
            try {
                double result = calculator.eval(input);
                out.println(result);
            }catch( Exception e){
                out.println(e.getMessage());
            }
        }
    }


}
