package cardit.palomares.javier.com.mycardit.card;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cardit.palomares.javier.com.mycardit.utils.BackupFileHandler;
import cardit.palomares.javier.com.mycardit.utils.FileBackupUtil;
import cardit.palomares.javier.com.mycardit.utils.FileDefinitionSource;
import cardit.palomares.javier.com.mycardit.utils.FileUtils;
import cardit.palomares.javier.com.mycardit.utils.XmlReader;
import cardit.palomares.javier.com.mycardit.utils.XmlWriter;

import org.w3c.dom.Element;
import java.lang.Long;
/**
 * Created by javierpalomares on 11/4/15.
 */
public class CardDatabase {

    private static final String ROOT_NODE = "cards";
    private static final String VERSION_NODE = "version";
    private static final String CARDS_NODE = "cards";
    private static final String CARD_NODE = "card";
    private static final String FIRST_NAME_NODE = "firstName";
    private static final String LAST_NAME_NODE = "lastName";
    private static final String IMG_FILE_NAME_NODE = "imgFileName";
    private static final String MY_CARD_NODE = "myCard";
    private static final String MY_FIRST_NAME_NODE = "myFirstName";
    private static final String MY_LAST_NAME_NODE = "myLastName";
    private static final String MY_IMG_FILE_NAME_NODE = "myImgFileName";
    private static final String DEFAULT_FILE_NAME = "/data/user/0/cardit.palomares.javier.com.my.cardit/files/cards.config";

    private static String filename;

    private long version = 0;
    private static volatile CardDatabase instance;
    private static Card myCard;
    public CardDatabase(){

    }

    public static CardDatabase getInstance(Context c){
        if (instance == null)
        {
            synchronized (CardDatabase.class)
            {
                instance = new CardDatabase();
                filename = c.getFilesDir().getAbsoluteFile() + "/cards.config";
                myCard = null;
            }
        }
        return instance;
    }


    /**
     * Open the databse file and parse its contents
     */
    public synchronized Set<Card> load()
    {
        CardDatabaseConfigHandler backupFileHandler = new CardDatabaseConfigHandler();
        FileBackupUtil.safeLoadFile(backupFileHandler);
        return backupFileHandler.cards;
    }

    public synchronized void addCard(Card card)
    {
        Set<Card> persistedCards = load();
        persistedCards.add(card);
        saveCards(persistedCards);
    }

    public synchronized  void setMyCard(Card card)
    {
        myCard = card;
    }

    public synchronized void removeCard(Card card)
    {
        Set<Card> persistedCards = load();
        persistedCards.remove(card);
        saveCards(persistedCards);
    }

    private synchronized void saveCards(Set<Card> cards)
    {
        //inititate peristance to xml file
        CardDatabaseConfigHandler backupFileHandler = new CardDatabaseConfigHandler();
        backupFileHandler.cards = cards;
        backupFileHandler.myCard = myCard;
        FileBackupUtil.safeWriteFile(backupFileHandler);
    }

    private class CardDatabaseConfigHandler implements BackupFileHandler
    {
        private Set<Card> cards;
        private Card myCard;

        @Override
        public String getDefaultFilename(){
            return DEFAULT_FILE_NAME;
        }

        @Override
        public int getMinConfigLength(){
            return 0;
        }
        @Override
        public void saveFile(File file)
        {
            XmlWriter out = null;
            try
            {
                out = new XmlWriter();
            }
            catch (Exception e)
            {
                return;
            }
            version++;
            Element root = out.setRootNode(ROOT_NODE);
            out.addNode(root, VERSION_NODE, Long.toString(version));
            Element myCardNode = out.addNode(root,MY_CARD_NODE);
            out.addNode(myCardNode,MY_FIRST_NAME_NODE,myCard.getFirstName());
            out.addNode(myCardNode,MY_LAST_NAME_NODE,myCard.getLastName());
            out.addNode(myCardNode,MY_IMG_FILE_NAME_NODE,myCard.getImgFileName());

            Element cardsNode = out.addNode(root,CARDS_NODE);

            for (Card card: cards)
            {
                try {
                    Element node = out.addNode(cardsNode,CARD_NODE);

                    //save the firstNme
                    out.addNode(node,FIRST_NAME_NODE,card.getFirstName());
                    //save the last name
                    out.addNode(node,LAST_NAME_NODE,card.getLastName());
                    //save the img file path
                    out.addNode(node,IMG_FILE_NAME_NODE,card.getImgFileName());

                }catch (Exception e)
                {
                    //TODO: Log to logcat
                }
            }
            try {
                out.saveDoc(file);
            }catch(Exception e)
            {
                //TODO: Log to logcat
            }
        }

        @Override
        public boolean readFile(File file)
        {
            boolean success = true;

            cards = new HashSet<Card>();
            myCard = null;

            FileDefinitionSource src = new FileDefinitionSource(file);
            XmlReader reader = null;

            try
            {
                reader = new XmlReader(src);
                success = load(reader);
            }catch (Exception e)
            {
                success = false;
                //TODO: Log to logcat
            }
            finally{
                //cleanup
                FileUtils.closeAndIgnoreFail(src);
                FileUtils.closeAndIgnoreFail(reader);
            }
            return success;
        }

        private boolean generateConfigFile()
        {
            XmlWriter out = null;
            try{
                out = new XmlWriter();
            }
            catch(Exception e)
            {
                // TODO: Log to logcat
                return false;
            }
            //reinitiaze cards
            cards = new HashSet<Card>();
            version = 1;
            Element root = out.setRootNode(ROOT_NODE);
            out.addNode(root,VERSION_NODE,Long.toString(version));

            Element cardsNode = out.addNode(root,CARDS_NODE);

            File file = new File(filename);

            //Empty cards
            try{
                out.saveDoc(file);
            }catch(Exception e)
            {
                //TODO: Log error
                file.delete();
                return false;
            }
            return true;
        }

        @Override
        public String getFileName()
        {
            return filename;
        }

        private boolean load(XmlReader reader)
        {
            Element root = reader.getRoot();

            //read the contents
            List<Element> kids = reader.getChildNodes(root);
            for (int i = 0; i < kids.size();i++)
            {
                Element curr = kids.get(i);
                String currName = XmlReader.getNodeName(curr);
                if(currName.equals(VERSION_NODE))
                {
                    version = reader.getNodeContentsLong(curr,1);
                    continue;
                }
                else if (currName.equals(MY_CARD_NODE)){
                    loadMyCard(curr,reader);
                }
                else if (currName.equals(CARDS_NODE))
                {
                    List<Element> cardNodes = reader.getChildNodes(curr);
                    for (int j=0;i <cardNodes.size();j++)
                    {
                        Element cardNode = cardNodes.get(j);
                        loadCard(cardNode,reader);
                    }
                }
            }
            return true;
        }

        public boolean setMyCard(Card toSet){
            myCard = toSet;
            return true;
        }

        private void loadMyCard(Element myCardNode,XmlReader reader){
            Card card = null;
            String myFirstName = null;
            String myLastName = null;
            String myFilePath = null;
            Bitmap myImg = null;

            List<Element> kids = reader.getChildNodes(myCardNode);
            for (int i = 0;i<kids.size();i++) {
                Element curr = kids.get(i);
                String currName = XmlReader.getNodeName(curr);

                if(currName.equals(MY_FIRST_NAME_NODE))
                {
                    myFirstName = reader.getNodeContents(curr);
                }else if (currName.equals(MY_LAST_NAME_NODE))
                {
                    myLastName = reader.getNodeContents(curr);

                }else if(currName.equals(MY_IMG_FILE_NAME_NODE))
                {
                    myFilePath = reader.getNodeContents(curr);
                }
                else{
                    //TODO: Log error to logcat
                }
            }

            if (myFirstName != null && myLastName != null && myFilePath != null)
            {
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Bitmap.Config.ARGB_8888;
                myImg =  BitmapFactory.decodeFile(myFilePath);
                card = new Card(myFirstName,myLastName,myImg,myFilePath);
            }

            if (card != null){
                myCard = card;
            }else{
                //TODO: Log error to logcat
            }
        }

        private void loadCard(Element cardNode,XmlReader reader)
        {
            Card card = null;
            String firstName = null;
            String lastName = null;
            String filePath = null;
            Bitmap img = null;

            List<Element> kids = reader.getChildNodes(cardNode);
            for (int i = 0;i<kids.size();i++) {
                Element curr = kids.get(i);
                String currName = XmlReader.getNodeName(curr);

                if(currName.equals(FIRST_NAME_NODE))
                {
                    firstName = reader.getNodeContents(curr);
                }else if (currName.equals(LAST_NAME_NODE))
                {
                    lastName = reader.getNodeContents(curr);

                }else if(currName.equals(IMG_FILE_NAME_NODE))
                {
                    filePath = reader.getNodeContents(curr);
                }
                else{
                    //TODO: Log error to logcat
                }
            }

            if (firstName != null && lastName != null && filePath != null)
            {
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Bitmap.Config.ARGB_8888;
                img =  BitmapFactory.decodeFile(filePath);
                card = new Card(firstName,lastName,img,filePath);
            }
            if( card != null)
            {
                cards.add(card);
            }else{
                //TODO: Log error to logcat
            }
        }
        @Override
        public boolean restoreDefaultFileFromMemory()
        {
            generateConfigFile();
            return false;
        }



    }


}
