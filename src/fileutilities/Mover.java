package fileutilities;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.SwingWorker;

//Данный класс используется для реализации копирования и перемещения объектов
public class Mover extends SwingWorker<Void, Void>{

    //Перечень значений опций копирования
    public static final int COPY_OPT=1;
    public static final int MOVE_OPT=2;

    //Перечень основных параметров работы класса
    private final File sourceFolder;
    private final File[] source;
    private final File targetFolder;
    private final int copyOption;

    //Список объектов из source, которые после выполнения процедуры копирования/перемещения надо сделать выделенными в папке targetFolder
    private final LinkedList<File> targetAllocateList=new LinkedList<>();

    //Список полей, необходимых для построения окна копирования
    private static JFrame copyFrame=null;
    private static final JLabel copyLabel=new JLabel("");
    private static String titleCopyFrame="";
    private static String copyLabelPrefix="";

    //Список полей, необходимых для отображения диалога конфликта файлов
    private static JDialog conflictDialog=null;
    private static JLabel conflictLabel=new JLabel("Файл: ");
    private static JComboBox<String> conflictVariants=new JComboBox<>(new String[]{"Не копировать этот файл", "Заменить файл в папке назначения", "Скопировать, но сохранить оба файла"});
    private static JCheckBox conflictDefaultBox=new JCheckBox("Выполнить это действие для всех конфликтов", true);
    private static JButton okBtn=new JButton("OK");

    private static final int NO_COPY=0;
    private static final int REPLACE_FILE=1;
    private static final int SAVE_BOTH_FILES=2;

    private boolean isShowConflictDialog=true;        //Если равен true, то в случае конфликта имен файлов программа показывает диалог с запросом о разрешении конфликта, в противном случае выбирается действие по-умолчанию
    private int conflictDefaultAct=REPLACE_FILE;      //Действие по-умолчанию в случае конфликта имен файлов

    //Поля, необходимые непосредственно для копирования файлов и папок
    private LinkedList<File> s=new LinkedList<>();        //Список источников
    private LinkedList<File> t=new LinkedList<>();        //Список приемников
    private LinkedList<String> err=new LinkedList<>();    //Список ошибок

    //Поле, необходимое для последующего корректного отображения результатов операции в панелях окна
    private LinkedList<File> allocateTarget=new LinkedList<>();

    public Mover (File sourceFolder, File[] source, File targetFolder, int copyOption){
        this.sourceFolder=sourceFolder;
        this.source=source;
        this.targetFolder=targetFolder;
        this.copyOption=copyOption;

        //Создаем элементы интерфейса окна копирования
        if(copyOption==COPY_OPT){
            titleCopyFrame="Копирование";
            copyLabelPrefix="Копирую: ";
        }
        if(copyOption==MOVE_OPT){
            titleCopyFrame="Перемещение";
            copyLabelPrefix="Перемещаю: ";
        }

        //Создаем диалог копирования
        if(copyFrame==null){
            int frameWidth=400;
            int frameHeight=100;
            copyFrame=new JFrame(titleCopyFrame);
            copyFrame.setSize(frameWidth, frameHeight);
            copyFrame.setResizable(false);
            copyFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            int xPos=Toolkit.getDefaultToolkit().getScreenSize().width/2-frameWidth/2;
            int yPos=Toolkit.getDefaultToolkit().getScreenSize().height/2-frameHeight/2;
            copyFrame.setLocation(xPos, yPos);
            copyFrame.setLayout(new FlowLayout());
            Box p0=Box.createVerticalBox();
            Box p1=Box.createHorizontalBox();
            p1.add(copyLabel);
            p1.add(Box.createHorizontalGlue());
            p0.add(p1);
            p0.add(Box.createVerticalGlue());
            copyFrame.setContentPane(p0);
        }
        copyFrame.setTitle(titleCopyFrame);
        copyLabel.setText(copyLabelPrefix+"...");

        //Создаем диалог, который будет отображаться в случае конфликта имен файлов
        if(conflictDialog==null){
            int dialogWidth=350;
            int dialogHeight=200;
            conflictDialog=new JDialog(copyFrame, true);
            conflictDialog.setTitle("Внимание");
            conflictDialog.setSize(new Dimension(dialogWidth, dialogHeight));
            conflictDialog.setResizable(false);
            conflictDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            int xPos=Toolkit.getDefaultToolkit().getScreenSize().width/2-dialogWidth/2;
            int yPos=Toolkit.getDefaultToolkit().getScreenSize().height/2-dialogHeight/2;
            conflictDialog.setLocation(xPos, yPos);

            Box p0=Box.createVerticalBox();
            p0.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            Box p1=Box.createHorizontalBox();
            p1.add(conflictLabel);
            p1.add(Box.createHorizontalGlue());
            p0.add(p1);

            p0.add(Box.createVerticalGlue());

            Box p2=Box.createHorizontalBox();
            p2.add(new JLabel("Выберите действие:"));
            p2.add(Box.createHorizontalGlue());
            p0.add(p2);

            Box p3=Box.createHorizontalBox();
            conflictVariants.setMaximumSize(new Dimension(dialogWidth-20, 20));
            p3.add(conflictVariants);
            p0.add(p3);

            p0.add(Box.createVerticalStrut(10));

            Box p4=Box.createHorizontalBox();
            p4.add(conflictDefaultBox);
            p4.add(Box.createHorizontalGlue());
            p0.add(p4);

            Box p5=Box.createHorizontalBox();
            p5.add(Box.createHorizontalGlue());
            p5.add(okBtn);
            p0.add(p5);

            conflictDialog.setContentPane(p0);
            okBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    conflictDialog.setVisible(false);
                }
            });

        }

    }

    @Override
    protected Void doInBackground() throws Exception {

        //проверяем тривиальные условия
        if(source.length==0){
            firePropertyChange("exceptMove", null, null);
            return null;
        }
        if(!sourceFolder.exists()){
            JOptionPane.showMessageDialog(null, "<html>Папка: "+sourceFolder.getAbsolutePath()+" не существует или перестала быть доступна.<br>Проверьте расположение папки", "Внимание", JOptionPane.INFORMATION_MESSAGE);
            firePropertyChange("exceptMove", null, null);
            return null;
        }
        if(!targetFolder.exists()){
            JOptionPane.showMessageDialog(null, "<html>Папка: "+targetFolder.getAbsolutePath()+" не существует или перестала быть доступна.<br>Проверьте расположение папки", "Внимание", JOptionPane.INFORMATION_MESSAGE);
            firePropertyChange("exceptMove", null, null);
            return null;
        }

        //Отображаем окно копирования
        int xPos=Toolkit.getDefaultToolkit().getScreenSize().width/2-copyFrame.getSize().width/2;
        int yPos=Toolkit.getDefaultToolkit().getScreenSize().height/2-copyFrame.getSize().height/2;
        copyFrame.setLocation(xPos, yPos);
        copyFrame.setVisible(true);

        //Формируем список источников
        Walker walker=new Walker() {

            @Override
            public FileVisitResult preVisitDirectory(Path f, BasicFileAttributes atr) throws IOException{
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path f, IOException ex) throws IOException{
                s.add(f.toFile());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes atr) throws IOException{
                s.add(f.toFile());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path f, IOException exc) throws IOException{
                err.add("<html>Нет доступа к "+f.toString()+"</html>");
                return FileVisitResult.CONTINUE;
            }

        };
        for(File f: source){
            if(!f.exists()){
                err.add("<html>Объект "+f.getAbsolutePath()+" не существует</html>");
                continue;
            }
            if(f.isFile()){
                s.add(f);
                continue;
            }
            if(f.isDirectory()){
                try{
                    Files.walkFileTree(f.toPath(), walker);
                }catch(IOException ex){}
            }
        }

        //Список источников не должен быть пуст
        if(s.isEmpty()){
            JOptionPane.showMessageDialog(null, "<html>Выбранные Вами объеты перемещены, удалены, либо у Вас нет прав доступа к ним", "Внимание", JOptionPane.INFORMATION_MESSAGE);
            firePropertyChange("exceptMove", null, null);
            return null;
        }

        //Формируем список приемников
        int pos=sourceFolder.getAbsolutePath().length();
        for(File f: s){
            t.add(new File(targetFolder.getAbsolutePath()+File.separator+f.getAbsolutePath().substring(pos)));
        }

        //Делаем "снимок" targetFolder до копирования
        LinkedList<File> al0=new LinkedList<>();
        al0.addAll(Arrays.asList(targetFolder.listFiles()));

        //Начинаем копирование
        File fs, ft, p;
        for(int i=0;i<s.size();i++){
            fs=s.get(i);
            ft=t.get(i);

            //Отображаем элемент, который будем обрабатывать
            copyLabel.setText("<html>"+copyLabelPrefix+fs.getAbsolutePath()+"</html>");

            if(!fs.exists())continue;

            //Если копируем папку...
            if(fs.isDirectory()){
                if(!ft.exists())if(!ft.mkdirs()){
                    err.add("<html>Не удалось создать папку "+ft.getAbsolutePath()+"</html>");
                    continue;
                }
                if(copyOption==MOVE_OPT){
                    if(ft.exists())if(!fs.delete()){
                        err.add("Папка "+ft.getAbsolutePath()+" была скопирована либо уже существовала в папке назначения, но удалить ее в папке источнике не удалось");
                    }
                }
                continue;
            }

            //Если копируем файл...
            if(fs.isFile()){
                p=(ft.getParentFile()==null)?(ft.toPath().getRoot().toFile()):(ft.getParentFile());
                if(!p.exists()){
                    if(!p.mkdirs()){
                        err.add("<html>не удалось создать папку "+p.getAbsolutePath()+"</html>");
                        continue;
                    }
                }
                try {
                    //Если файл уже существует и значение флага показа диалога конфликтов равно true, то отображаем диалог конфликта
                    if(ft.exists()){
                        if(isShowConflictDialog){
                            conflictLabel.setText("<html>Файл: "+ft.getName()+" уже существует в папке назначения</html>");
                            conflictVariants.setSelectedIndex(0);
                            xPos=Toolkit.getDefaultToolkit().getScreenSize().width/2-conflictDialog.getSize().width/2;
                            yPos=Toolkit.getDefaultToolkit().getScreenSize().height/2-conflictDialog.getSize().height/2;
                            conflictDialog.setLocation(xPos, yPos);
                            conflictDialog.setVisible(true);
                            if(conflictDefaultBox.isSelected())isShowConflictDialog=false;
                            if(conflictVariants.getSelectedIndex()==0)conflictDefaultAct=NO_COPY;
                            if(conflictVariants.getSelectedIndex()==1)conflictDefaultAct=REPLACE_FILE;
                            if(conflictVariants.getSelectedIndex()==2)conflictDefaultAct=SAVE_BOTH_FILES;
                        }
                        if(conflictDefaultAct==NO_COPY)continue;
                        if(conflictDefaultAct==SAVE_BOTH_FILES){
                            String fullName;
                            String name;
                            String ext;
                            int count=0;
                            fullName=ft.getName();
                            name=getNameFile(fullName);
                            ext=getExtendFile(fullName);
                            do{
                                count++;
                                ft=new File(p.getAbsolutePath()+File.separator+name+"("+count+")."+ext);
                            }while(ft.exists());
                            t.set(i, ft);
                        }
                    }
                    if(copyOption==COPY_OPT)Files.copy(fs.toPath(), ft.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if(copyOption==MOVE_OPT)Files.move(fs.toPath(), ft.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    if(copyOption==COPY_OPT)err.add("<html>Не удалось скопировать файл "+fs.getAbsolutePath()+"</html>");
                    if(copyOption==MOVE_OPT)err.add("<html>Не удалось переместить файл "+fs.getAbsolutePath()+"</html>");
                }
            }

        }

        //Скрываем окно копирования
        copyFrame.setVisible(false);

        //Делаем "снимок" targetFolder после копирования
        LinkedList<File> al1=new LinkedList<>();
        al1.addAll(Arrays.asList(targetFolder.listFiles()));

        //В список allocateTarget добавляем те объекты, которых небыло в "снимке" al0, но которые есть в t
        boolean isFindInT;
        boolean isFindInAl0;
        for(File f: al1){
            isFindInAl0=false;
            for(File j: al0){
                if(f.equals(j)){
                    isFindInAl0=true;
                    break;
                }
            }
            isFindInT=false;
            for(File j: t){
                if(f.equals(j)){
                    isFindInT=true;
                    break;
                }
            }
            if(!isFindInAl0 & isFindInT)allocateTarget.add(f);
        }

        //В случае возникновения ошибок в процессе копирования уведомляем пользователя
        if(!err.isEmpty()){
            Box p0=Box.createVerticalBox();
            Box p1=Box.createHorizontalBox();
            Box p2=Box.createVerticalBox();
            for(String str: err){
                p2.add(new JLabel(str));
                p2.add(Box.createVerticalStrut(5));
            }
            JScrollPane sp=new JScrollPane(p2);
            sp.setPreferredSize(new Dimension(500, 400));
            p1.add(new JLabel("Во время "+((copyOption==COPY_OPT)?("копирования"):("перемещения"))+" возникли следующие ошибки:"));
            p1.add(Box.createHorizontalGlue());
            p0.add(p1);
            p0.add(Box.createVerticalStrut(10));
            p0.add(sp);
            JOptionPane.showMessageDialog(null, p0, "Внимение", JOptionPane.INFORMATION_MESSAGE);
        }

        //Уведомляем слушателя в MainClass о том, что процесс копирования завершен
        firePropertyChange("endMover", null, null);
        return null;
    }

    //Метод возвращает список объектов который должен быть выделен в папке sourceFolder
    public File[] getAllocateSource(){
        return source;
    }

    //Метод возвращает список объектов, который должен быть выделен в папке targetFolder
    public File[] getAllocateTarget(){
        File[] result;
        result=new File[allocateTarget.size()];
        result=allocateTarget.toArray(result);
        return result;
    }

    // ------------------------- Вспомогательные методы -------------------------

    //Метод возвращает имя файла, отделенное от расширения
    private static String getNameFile(String nameFile){
        String result="";
        String ext=getExtendFile(nameFile);
        if(ext.equals(""))result=nameFile; else result=nameFile.substring(0, nameFile.lastIndexOf("."+ext));
        return result;
    }

    //Метод возвращает расширение файла nameFile. Если расширения нет - возвращает пустую строку.
    //Расширением считается последовательность символов после последней точки.
    private static String getExtendFile(String nameFile){
        String extendFile="";
        int dotPos=nameFile.lastIndexOf(".");
        if((dotPos==(-1)) | (dotPos==0) | (dotPos==(nameFile.length()-1)))extendFile=""; else extendFile=nameFile.substring(dotPos+1);
        return extendFile.toLowerCase();
    }

    //Класс необходим для реализации обхода дерева папок
    private static abstract class Walker extends SimpleFileVisitor<Path>{}

}
