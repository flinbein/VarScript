package ru.dpohvar.varscript.writer;


/**
 * Created by DPOH-VAR on 02.03.14
 */
public class SendBufferTask implements Runnable{

    private final CommandSenderWriter writer;

    public SendBufferTask(CommandSenderWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {
        synchronized (writer) {
            String[] strings = writer.builder.toString().split("\n");
            writer.builder = new StringBuilder();
            writer.task = null;
            for (String s: strings) {
                if (s.endsWith("\r")) s = s.substring(0,s.length()-1);
                s = s.replace("\t","  ");
                writer.sender.sendMessage(writer.prefix + s);
            }
        }

    }
}
