package fileutilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

//Класс, необходимый для получения свойств объектов
public class Properties {

    private static String name;          //Переменная для хранения имен объектов
    private static String type;          //Переменная для хранения типов объектов
    private static double size;          //Переменная для хранения размеров объектов
    private static double size_tmp;      //Переменная для хранения суммарного размера файлов подгруппы
    private static Date dCreate;         //Переменная для хранения дат создания объектов
    private static Date dOpen;           //Переменная для хранения дат открытия объектов
    private static Date dModif;          //Переменная для хранения дат последней модификации объектов
    private static int countFiles;       //Переменная для хранения количества файлов
    private static int countFiles_tmp;   //Переменная для хранения количества файлов в подгруппе
    private static int countFolders;     //Переменная для хранения количества папок
    private static int countFolders_tmp; //Переменная для хранения количества папок в подгруппе

    public static void showProperties(File[] f){
        //Обнуляем содержимое числовых переменных
        size=0;
        countFiles=0;
        countFolders=0;

        //Действие, выполняемое когда массив объектов пуст
        if(f.length==0)return;

        //Элементы интерфейса для отображения свойств объектов
        JPanel msgPanel=new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        JPanel[] rowPane=new JPanel[10];
        JLabel[] lSigns=new JLabel[10];        //Метки с наименованиями параметров
        JLabel[] lVals=new JLabel[10];         //Метки со значениями параметров

        //Объекты, необходимые для форматирования выводимых на экран числовых значений
        DateFormat df=DateFormat.getInstance();
        NumberFormat nfLong=NumberFormat.getInstance();
        NumberFormat nfShort=NumberFormat.getInstance();
        nfShort.setMaximumFractionDigits(2);

        //Действие, выполняемое, когда массив содержит один элемент
        if(f.length==1){

            //Проверяем, существует ли еще данный объект
            if(!f[0].exists()){
                JOptionPane.showMessageDialog(null, "<html>Невозможно получить свойства объекта.<br>Возможно он был перемещен или удален,<br>либо у Вас нет прав на доступ к нему.", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            //Вспомогательная переменная для доступа к атрибутам файлов и папок
            DosFileAttributes atr=null;

            //Если данный объект - файл
            if(f[0].isFile()){
                try {
                    name=f[0].getName();
                    type=getExtendFile(name);
                    size=f[0].length();
                    atr=Files.readAttributes(f[0].toPath(), DosFileAttributes.class);
                    dCreate=new Date(atr.creationTime().toMillis());
                    dOpen=new Date(atr.lastAccessTime().toMillis());
                    dModif=new Date(atr.lastModifiedTime().toMillis());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "<html>Невозможно получить свойства объекта.<br>Возможно он был перемещен или удален,<br>либо у Вас нет прав на доступ к нему.", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                lSigns[0]=new JLabel("Имя:"); lVals[0]=new JLabel(name);
                lSigns[1]=new JLabel("Тип:"); lVals[1]=new JLabel(type);
                lSigns[2]=new JLabel("Размер:"); lVals[2]=new JLabel(nfLong.format(new Long((long)size))+" байт ( "+nfShort.format(size/(size>=1073741824?1073741824:1048576))+" "+(size>=1073741824?"Гб":"Мб")+")");
                lSigns[3]=new JLabel("Дата создания:"); lVals[3]=new JLabel(df.format(dCreate));
                lSigns[4]=new JLabel("Дата открытия:"); lVals[4]=new JLabel(df.format(dOpen));
                lSigns[5]=new JLabel("Дата изменения:"); lVals[5]=new JLabel(df.format(dModif));
                if(atr.isHidden()){
                    lSigns[6]=new JLabel("Файл скрыт"); lVals[6]=new JLabel("");
                }
                if(atr.isReadOnly()){
                    lSigns[7]=new JLabel("Файл только для чтения"); lVals[7]=new JLabel("");
                }
            }

            //Если данный объект - папка
            if(f[0].isDirectory()){
                try {
                    name=f[0].getName();
                    type="Папка";
                    //Следующая строка кода подсчитывает количество и общий размер файлов в папке и количество подпапок в ней
                    size_tmp=0;
                    countFiles_tmp=0;
                    countFolders_tmp=0;
                    Files.walkFileTree(f[0].toPath(), new Walker(f[0]));
                    size=size_tmp;
                    countFiles=countFiles_tmp;
                    countFolders=countFolders_tmp;
                    atr=Files.readAttributes(f[0].toPath(), DosFileAttributes.class);
                    dCreate=new Date(atr.creationTime().toMillis());
                    dOpen=new Date(atr.lastAccessTime().toMillis());
                    dModif=new Date(atr.lastModifiedTime().toMillis());

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "<html>Невозможно получить свойства объекта.<br>Возможно он был перемещен или удален,<br>либо у Вас нет прав на доступ к нему.", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                lSigns[0]=new JLabel("Имя:"); lVals[0]=new JLabel(name);
                lSigns[1]=new JLabel("Тип:"); lVals[1]=new JLabel(type);
                lSigns[2]=new JLabel("Размер:"); lVals[2]=new JLabel(nfLong.format(new Long((long)size))+" байт ( "+nfShort.format(size/(size>=1073741824?1073741824:1048576))+" "+(size>=1073741824?"Гб":"Мб")+")");
                lSigns[3]=new JLabel("Дата создания:"); lVals[3]=new JLabel(df.format(dCreate));
                lSigns[4]=new JLabel("Дата открытия:"); lVals[4]=new JLabel(df.format(dOpen));
                lSigns[5]=new JLabel("Дата изменения:"); lVals[5]=new JLabel(df.format(dModif));
                lSigns[6]=new JLabel("Содержит файлов:"); lVals[6]=new JLabel(nfLong.format(new Long((long)countFiles)));
                lSigns[7]=new JLabel("Содержит папок:"); lVals[7]=new JLabel(nfLong.format(new Long((long)countFolders)));
                if(atr.isHidden()){
                    lSigns[8]=new JLabel("Папка скрыта"); lVals[8]=new JLabel("");
                }
                if(atr.isReadOnly()){
                    lSigns[9]=new JLabel("Папка только для чтения"); lVals[9]=new JLabel("");
                }
            }

        }

        //Действие, выполняемое, когда массив содержит группу элементов
        if(f.length>1){
            //Список объектов, для которых не удалось получить свойства
            LinkedList<File> failedList=new LinkedList<>();
            //Объект, необходимый для обхода дерева каталогов
            Walker walker=new Walker();
            //Признак того, что в группе объектов есть файлы
            boolean isFileFind=false;
            //Признак того, что в группе есть каталоги
            boolean isFolderFind=false;

            for(File fTmp: f){
                //Если объект недоступен
                if(!fTmp.exists()){
                    failedList.add(fTmp);
                    continue;
                }
                //Если объект - это файл
                if(fTmp.isFile()){
                    isFileFind=true;
                    size+=fTmp.length();
                    countFiles++;
                    continue;
                }
                //Если объект - это папка
                if(fTmp.isDirectory()){
                    isFolderFind=true;
                    countFolders++;
                    try {
                        walker.setFolder(fTmp);
                        size_tmp=0;
                        countFiles_tmp=0;
                        countFolders_tmp=0;
                        Files.walkFileTree(fTmp.toPath(), walker);
                        size+=size_tmp;
                        countFiles+=countFiles_tmp;
                        countFolders+=countFolders_tmp;
                    } catch (IOException ex) {
                        failedList.add(fTmp);
                    }
                }
            }

            if(failedList.size()==f.length){
                JOptionPane.showMessageDialog(null, "<html>Невозможно отобразить свойства для выбранных Вами объектов.<br>Возможно они были перемещены, переименованы, либо у Вас нет прав на доступ к ним", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if(failedList.size()>0){
                String list="<html>Свойства следующих объектов получить не удалось:<br>";
                for(File fTmp: failedList)list+=fTmp.getAbsolutePath()+"<br>";
                list+="Возможно они были перемещены, переименованы, либо у Вас нет прав на доступ к ним.<br>Нажмите ОК для продолжения.";
                JOptionPane.showMessageDialog(null, list, "Внимание", JOptionPane.INFORMATION_MESSAGE);
            }

            lSigns[0]=new JLabel("Тип:");
            if(isFileFind & !isFolderFind)lVals[0]=new JLabel("Файлы");
            if(!isFileFind & isFolderFind)lVals[0]=new JLabel("Папки");
            if(isFileFind & isFolderFind)lVals[0]=new JLabel("Файлы и папки");
            lSigns[1]=new JLabel("Количество файлов:"); lVals[1]=new JLabel(nfLong.format(new Long((long)countFiles)));
            lSigns[2]=new JLabel("Количество папок:"); lVals[2]=new JLabel(nfLong.format(new Long((long)countFolders)));
            lSigns[3]=new JLabel("Общий размер:"); lVals[3]=new JLabel(nfLong.format(new Long((long)size))+" байт ( "+nfShort.format(size/(size>=1073741824?1073741824:1048576))+" "+(size>=1073741824?"Гб":"Мб")+")");

        }

        //Выводим результат работы на экран
        for(int i=0;i<lSigns.length;i++){
            if(lSigns[i]==null)continue;
            rowPane[i]=new JPanel();
            rowPane[i].setLayout(new BoxLayout(rowPane[i], BoxLayout.X_AXIS));
            rowPane[i].setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            lSigns[i].setHorizontalTextPosition(SwingConstants.LEFT);
            lVals[i].setHorizontalTextPosition(SwingConstants.RIGHT);
            rowPane[i].add(lSigns[i]);
            rowPane[i].add(Box.createHorizontalStrut(20));
            rowPane[i].add(Box.createHorizontalGlue());
            rowPane[i].add(lVals[i]);
            msgPanel.add(rowPane[i]);
        }
        JOptionPane.showMessageDialog(null, msgPanel, "Свойства", JOptionPane.INFORMATION_MESSAGE);

    }

    //Класс необходим для реализации обхода папок
    private static class Walker extends SimpleFileVisitor<Path>{

        private Path root;

        Walker(){
            root=null;
        }

        Walker(File r){
            root=r.toPath();
        }

        void setFolder(File r){
            root=r.toPath();
        }

        @Override
        public FileVisitResult visitFile(Path f, BasicFileAttributes atr) throws IOException{
            size_tmp+=atr.size();
            countFiles_tmp++;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path f, BasicFileAttributes atr) throws IOException{
            if(!f.equals(root))countFolders_tmp++;
            return FileVisitResult.CONTINUE;
        }

    }

    //Метод возвращает расширение файла nameFile. Если расширения нет - возвращает пустую строку.
    //Расширением считается последовательность символов после последней точки.
    private static String getExtendFile(String nameFile){
        String extendFile="";
        int dotPos=nameFile.lastIndexOf(".");
        if((dotPos==(-1)) | (dotPos==0) | (dotPos==nameFile.length()))extendFile=""; else extendFile=nameFile.substring(dotPos+1);
        return extendFile.toLowerCase();
    }

}
