package fileutilities;

import java.io.*;
import javax.swing.JOptionPane;

//Класс, необходимый для создания папок
public class Creater {

    //В метод передается текущий каталог той панели, в которой будем создавать папку
    //Метод возвращает объект File, описывающий созданную папку. Возвращает null, если папка не была создана
    public static File createFolder(File f){
        //Папка f должна существовать и быть доступной для записи
        if(!f.exists()){
            JOptionPane.showMessageDialog(null, "<html>Папка:<br>"+f.getAbsolutePath()+"<br>не доступна. Проверьте расположение папки и права доступа к ней", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        //Объявляем вспомогательные переменные
        String name=null;
        char[] disabledChars={'\\', '/', ':', '*', '?', '\"', '<', '>', '|'};
        boolean isFind=false;
        File f_new;
        while (true) {
            name=JOptionPane.showInputDialog(null, "Введине имя для новой папки", "Введите имя", JOptionPane.QUESTION_MESSAGE);
            //Если пользователь отказался от ввода имени или ввел пустое имя, интерпретируем это как отказ от ввода
            if(name==null)return null;
            name=name.trim();
            if(name.equals(""))return null;
            //Проверяем введенное имя на наличие запрещенных символов
            isFind=false;
            for(char c: name.toCharArray())
                for(char d: disabledChars){
                    if(c==d)isFind=true;
                }
            if(isFind){
                JOptionPane.showMessageDialog(null, "Введенное имя содержит недопустимые символы. Введите другое имя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            //Строим новый объект, описывающий создаваемую папку и выполняем последнюю проверку
            f_new=new File(f.getAbsolutePath()+File.separator+name);
            if(f_new.exists() & f_new.isDirectory()){
                JOptionPane.showMessageDialog(null, "Папка с таким именем уже существует. Введите другое имя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            break;
        }

        //Создаем папку
        boolean isCreated=f_new.mkdir();
        if(!isCreated){
            JOptionPane.showMessageDialog(null, "<html>Не удалось создать папку. Возможно отсутствуют права на запись в каталог "+f.getAbsolutePath()+"<br>Либо в данном каталоге уже существует файл с таким именем", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return f_new;
    }

}
