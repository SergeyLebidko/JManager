package fileutilities;

import java.io.File;
import java.util.LinkedList;
import javax.swing.*;

//Класс, необходимый для переименования объектов
public class Renamer {

    //Метод возвращает null, если переименование не произведено. Или возвращает список (не нулевой длины) объектов, переименование которых было выполнено
    public static File[] renameFile(File[] f){
        if(f.length==0)return null;
        if(f.length==1){
            File fTmp=R1(f[0]);
            if(fTmp==null) return null;
            return new File[]{fTmp};
        }
        if(f.length>1)return R2(f);
        return null;
    }

    //Метод, предназначенный для переименования еденичных объектов
    private static File R1(File f){
        //Проверяем доступность элемента
        if(!f.exists()){
            JOptionPane.showMessageDialog(null, "<html>Объект "+f.getAbsolutePath()+" недоступен.<br>Возможно он был переимещен или переименован.", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        //Объявляем вспомогательные переменные
        File fTmp=null;
        String name=null;

        while(true){
            //Запрашиваем новое имя для объекта
            if(f.isFile())name=getName(f.getName(), "<html>Введите имя.<br>Обратите внимание, что в случае смены расширения файл может оказаться недоступным");
            if(f.isDirectory())name=getName(f.getName(), "Введите имя папки");
            if(name==null)return null;

            fTmp=new File((f.getParent()==null)?(f.toPath().getRoot().toString()):(f.getParent())+File.separator+name);
            if(fTmp.exists()){
                JOptionPane.showMessageDialog(null, "Объект с таким именем уже существует", "Внимание", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }

            break;
        }

        //Пробуем переименовать
        boolean successfullRename=f.renameTo(fTmp);
        if(!successfullRename){
            JOptionPane.showMessageDialog(null, "<html>Не удалось переименовать папку.<br>Возможно она была удалена, либо перемещена. Либо у Вас нет прав на ее переименование", "Ошибка переименования", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        //Возвращаем результат - ссылку на переименованный объект
        return fTmp;

    }

    //Метод, предназначенный для переименования множества объектов
    private static File[] R2(File[] f){
        LinkedList<File> poolSuccess=new LinkedList<>();    //Пул успешно переименованных объектов
        LinkedList<File> poolFailed=new LinkedList<>();     //Пул объектов, которые переименовать не удалось

        //Вспомогательные переменные
        String name="";         //Строка, необходимая для запроса и хранения нового имени группы объектов
        int count=1;            //Счетчик, добавляемый к новому имени объекта в группе
        String ext="";          //Переменная для хранения расширений файлов
        File nf;                //Временная переменная для хранения новых путей файлов
        boolean successRename;  //Переменная, сигнализирующая об успешности переименования

        //Запрашиваем имя
        name=getName("", "Введите имя");
        if(name==null)return null;

        //Переименовываем объекты
        for(File fTmp: f){

            nf=null;

            //Первый частный случай - объект, который нужно переименовать, уже не существует
            if(!fTmp.exists()){
                poolFailed.add(fTmp);
                continue;
            }

            //Второй случай - объект - файл
            if(fTmp.isFile()){
                ext=getExtendFile(fTmp.getName());
                while (true) {
                    nf=new File( (fTmp.getParent()==null?fTmp.toPath().getRoot().toString():fTmp.getParent())+File.separator+name+"("+count+")"+(ext.equals("")?"":"."+ext) );
                    count++;
                    if(!nf.exists())break;
                }

            }

            //Третий случай - объект - папка
            if(fTmp.isDirectory()){
                while (true) {
                    nf=new File( (fTmp.getParent()==null?fTmp.toPath().getRoot().toString():fTmp.getParent())+File.separator+name+"("+count+")" );
                    count++;
                    if(!nf.exists())break;
                }
            }

            //Попытка переименования
            successRename=fTmp.renameTo(nf);
            if(!successRename){
                poolFailed.add(fTmp);
                continue;
            }
            poolSuccess.add(nf);

        }

        //Сообщаем о неудачных попытках переименования
        if(poolFailed.size()>0 & !poolSuccess.isEmpty()){
            String msg="<html>Не удалось переименовать следующие объекты:<br>";
            for(File fTmp: poolFailed){
                msg+=fTmp.getAbsolutePath()+"<br>";
            }
            JOptionPane.showMessageDialog(null, msg, "Внимание", JOptionPane.INFORMATION_MESSAGE);
        }

        if(poolSuccess.isEmpty()){
            JOptionPane.showMessageDialog(null, "Попытка переименования объектов не удалась", "Внимание", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        File[] result;
        result=new File[poolSuccess.size()];
        result=poolSuccess.toArray(result);

        return result;
    }

    //Метод запрашивает имя папки или файла у пользователя. Возвращает null, если пользователь отказался от ввода
    private static String getName(String startName, String msg){
        String name="";
        boolean isFind;
        char[] disabledChars={'\\', '/', ':', '*', '?', '\"', '<', '>', '|'};
        while (true) {
            name=JOptionPane.showInputDialog(null, msg, startName);
            if(name==null)return null;
            name=name.trim();
            if(name.equals(""))return null;
            //Проверяем наличие недопустимых символов
            isFind=false;
            for(char c: name.toCharArray())
                for(char d: disabledChars){
                    if(c==d)isFind=true;
                }
            if(isFind){
                JOptionPane.showMessageDialog(null, "Введенное имя содержит недопустимые символы. Введите другое имя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            break;
        }
        return name;
    }

    //Метод возвращает расширение файла nameFile. Если расширения нет - возвращает пустую строку.
    //Расширением считается последовательность символов после последней точки.
    private static String getExtendFile(String nameFile){
        String extendFile="";
        int dotPos=nameFile.lastIndexOf(".");
        if((dotPos==(-1)) | (dotPos==0) | (dotPos==(nameFile.length()-1)))extendFile=""; else extendFile=nameFile.substring(dotPos+1);
        return extendFile.toLowerCase();
    }

}
