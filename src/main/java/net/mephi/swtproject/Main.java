package net.mephi.swtproject;


import net.mephi.swtproject.components.Board;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Created by Acer on 16.01.2016.
 */
public class Main {

    public static void main(String[] args){
        Main main = new Main();
        main.start();
    }
    private final int WIDTH = 300;
    private final int HEIGHT = 300;
    private void start(){
        Display display = new Display();
        Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.CENTER);

        FillLayout layout = new FillLayout();
        shell.setLayout(layout);

        Board board = new Board(shell);

        shell.setText("Bubble battle");
        int borW = shell.getSize().x - shell.getClientArea().width;
        int borH = shell.getSize().y - shell.getClientArea().height;
        shell.setSize(WIDTH + borW, HEIGHT + borH);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        //Ресурсы операционной системы
        //должны быть освобождены
        display.dispose();
    }

}
