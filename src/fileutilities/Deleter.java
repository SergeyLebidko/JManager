package fileutilities;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import javax.swing.JOptionPane;

//Этот класс нужен для реализации удаления файлов
public class Deleter {

    private static final LinkedList<File> failedList=new LinkedList<>();

    public static void delete(File[] f){
        //Сперва обрабатываем случай, при котором переданный массив объектов - пуст
        if(f.length==0)return;

        //Последовательно обрабатываем объекты, которые нужно удалить
        boolean success;
        Walker walker=new Walker();
        for(File fTmp: f){
            //Если объект не существует, просто пропускаем его
            if(!fTmp.exists())continue;

            //Если объект является файлом, то пытаемся его удалить
            if(fTmp.isFile()){
                success=fTmp.delete();
                if(!success)failedList.add(fTmp);
                continue;
            }

            //Если объект является папкой, то пытаемся удалить его, удалив сперва все входящие в него объекты
            if(fTmp.isDirectory()){
                try {
                    Files.walkFileTree(fTmp.toPath(), walker);
                } catch (IOException ex) {}
                if(fTmp.exists())failedList.add(fTmp);
            }
        }

        if(failedList.size()>0){
            String str="<html>Данные объекты удалить не удалось:<br>";
            for(File fTmp: failedList){
                str+=fTmp.getAbsolutePath()+"<br>";
            }
            str+="Возможно у Вас нет прав на доступ к ним";
            JOptionPane.showMessageDialog(null, str, "Внимание", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    //Класс необходим для реализации обхода и последовательного удаления объектов
    private static class Walker extends SimpleFileVisitor<Path>{

        @Override
        public FileVisitResult postVisitDirectory(Path f, IOException ex){
            f.toFile().delete();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path f, BasicFileAttributes atr) throws IOException{
            f.toFile().delete();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path f, IOException exc){
            return FileVisitResult.CONTINUE;
        }

    }

}
