package net.mephi.client;

import net.mephi.client.components.Ball;
import net.mephi.client.components.Board;
import net.mephi.client.components.InputDialog;
import net.mephi.server.Client;
import net.mephi.server.MultipleSocketServer;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Created by Snoll on 12.05.2016.
 */
public class ClientLauncher {

    private Logger log = Logger.getLogger(ClientLauncher.class);
    public static void main(String[] args) {
        ClientLauncher main = new ClientLauncher();
        main.start();
    }


    private void start() {
        Display display = new Display();
        Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.CENTER);
        shell.setText("Bubble battle");
        shell.setSize(Board.WIDTH , Board.HEIGHT );

        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = shell.getBounds();

        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;

        shell.setLocation(x, y);
        createTopMenu(shell);
        FillLayout layout = new FillLayout();
        shell.setLayout(layout);




        Board board = new Board(shell);
        shell.open();
        InputDialog dlg = new InputDialog(shell);

        String input = dlg.open();
        if (input == null) {
            input = "";
        }else{
            input = input.substring(0,Math.min(5,input.length()));
        }
        Client client = new Client();
        Ball ball = new Ball(input);
        client.setBall(ball);






        connectToServer(client);
        client.setBoard(board);
        client.startGame();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        log.debug("End game 4 "+client);
        //Ресурсы операционной системы
        //должны быть освобождены
        client.endGame();
        display.dispose();
    }


    private void connectToServer(Client client) {
        try {
            Socket clientSocket = new Socket("192.168.1.67", MultipleSocketServer.getPort());
            OutputStream os = clientSocket.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            client.setSocket(clientSocket);
            client.setOos(out);
            //Отправляем имя шарика и цвет

            String sendNameAndColor = client.getBall().getName() + "_" + client.getBall().getColor().getRed() + "_" + client.getBall().getColor().getGreen() + "_" + client.getBall().getColor().getBlue();
            out.writeObject(sendNameAndColor);
            out.flush();
out.reset();
            //Получаем уникальный ID
            InputStream is = clientSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            client.setOis(ois);
            String uuid = (String) ois.readObject();
            client.setUUID(uuid);
            log.debug("Connected to server; ID="+client.getUUID());


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void createTopMenu(Shell shell) {
        // Создание системы меню
        Menu main = createMenu(shell, SWT.BAR | SWT.LEFT_TO_RIGHT);
        shell.setMenuBar(main);

        MenuItem fileMenuItem = createMenuItem(main, SWT.CASCADE, "&Файл",
                null, -1, true, null, shell);
        Menu fileMenu = createMenu(shell, SWT.DROP_DOWN, fileMenuItem, true);
        MenuItem exitMenuItem = createMenuItem(fileMenu, SWT.PUSH, "&Выход\tCtrl+X",
                null, SWT.CTRL + 'X', true, "doExit", shell);

        MenuItem helpMenuItem = createMenuItem(main, SWT.CASCADE, "&Помощь",
                null, -1, true, null, shell);
        Menu helpMenu = createMenu(shell, SWT.DROP_DOWN, helpMenuItem, true);
        MenuItem aboutMenuItem = createMenuItem(helpMenu, SWT.PUSH, "&Об игре\tCtrl+A",
                null, SWT.CTRL + 'A', true, "doAbout", shell);
    }

    protected Menu createMenu(Shell parent, int style) {
        Menu m = new Menu(parent, style);
        return m;
    }

    protected Menu createMenu(Shell parent, int style,
                              MenuItem container, boolean enabled) {
        Menu m = createMenu(parent, style);
        m.setEnabled(enabled);
        container.setMenu(m);
        return m;
    }

    protected MenuItem createMenuItem(Menu parent, int style, String text,
                                      Image icon, int accel, boolean enabled,
                                      String callback, Shell shell) {
        MenuItem mi = new MenuItem(parent, style);
        if (text != null) {
            mi.setText(text);
        }
        if (icon != null) {
            mi.setImage(icon);
        }
        if (accel != -1) {
            mi.setAccelerator(accel);
        }
        mi.setEnabled(enabled);
        if (callback != null) {
            registerCallback(mi, this, callback, shell);
        }
        return mi;
    }

    protected void registerCallback(final MenuItem mi,
                                    final Object handler,
                                    final String handlerName, Shell shell) {
        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {

                    Class[] cArg = {Shell.class};
                    Method m = handler.getClass().
                            getMethod(handlerName, cArg[0]);
                    m.invoke(handler, shell);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void doAbout(Shell shell) {
        MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        mb.setText("BubbleBattle");
        mb.setMessage("Классная игра, неправда ли?");
        int rc = mb.open();
    }

    public void doExit(Shell shell) {
        System.exit(0);
    }

}
