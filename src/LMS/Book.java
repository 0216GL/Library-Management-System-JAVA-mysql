
package LMS;

import java.io.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static LMS.HoldRequestOperations.holdRequests;

public class Book {

    private final String isbn;    //ISBN是图书的唯一标识符（国际标准书号）
    private String title;         // 书名
    private String subject;       // 类目
    private String author;        // 作者
    private boolean isIssued;        // 是否正在外借中
  
    public Book(String isbn, String t, String s, String a, boolean issued)
    {
        this.isbn = isbn;
        title = t;
        subject = s;
        author = a;
        isIssued = issued;
    }


    // 打印所有图书预约列表
    public void printHoldRequests(HoldRequestOperations list)
    {
        if (!list.isEmpty())
        { 
            System.out.println("\n现存所有图书预约列表为: ");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");            
            System.out.println("ISBN\t\t书名\t\t\t预约者\t\t\t预约日期");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");
            
            for (int i = 0; i < list.size(); i++)
            {                      
                System.out.print(i + "-" + "\t\t");
                list.get(i).print();
            }
        }
        else
            System.out.println("\n没有正在进行的预约。");
    }
    
    // 打印图书信息
    public void printInfo()
    {
        System.out.println(title + "\t\t\t" + author + "\t\t\t" + subject);
    }
    
    // 改变图书信息
    public void changeBookInfo() throws IOException
    {
        String input;
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("\n更新作者? (y/n)");
        input = reader.readLine();
        
        if ("y".equals(input))
        {
            System.out.println("\n请输入新作者: ");
            String newAuthor = reader.readLine();
            if (newAuthor != null && !newAuthor.trim().isEmpty())
            {
                author = newAuthor;
            }
        }

        System.out.println("\n更新类目? (y/n)");
        input = reader.readLine();
        
        if ("y".equals(input))
        {
            System.out.println("\n请输入新类目: ");
            String newSubject = reader.readLine();
            if (newSubject != null && !newSubject.trim().isEmpty())
            {
                subject = newSubject;
            }
        }

        System.out.println("\n更新书名? (y/n)");
        input = reader.readLine();
        
        if ("y".equals(input))
        {
            System.out.println("\n请输入新书名: ");
            String newTitle = reader.readLine();
            if (newTitle != null && !newTitle.trim().isEmpty())
            {
                title = newTitle;
            }
        }        
        
        System.out.println("\n图书信息更新成功。");
        
    }
    
    /*------------Getter FUNCs.---------*/
    
    public String getTitle()
    {
        return title;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getAuthor()
    {
        return author;
    }
    
    public boolean getIssuedStatus()
    {
        return isIssued;
    }
    
    public void setIssuedStatus(boolean s)
    {
        isIssued = s;
    }
    
    public String getISBN()
    {
        return isbn;
    }

    public ArrayList<HoldRequest> getHoldRequests()
    {
        return holdRequests;
    }
    /*-----------------------------------*/

    
    //-------------------------------------------------------------------//

    // 创建一个"预约请求"
    public void placeBookOnHold(Borrower bor)
    {
        HoldRequest hr = new HoldRequest(bor,this, new Date());

        HoldRequestOperations.addHoldRequest(hr);
        bor.addHoldRequest(hr);
        
        System.out.println("\nThe book " + title + " has been successfully placed on hold by borrower " + bor.getName() + ".\n");
    }
    
    

    //  图书预约/保留请求
    public void makeHoldRequest(Borrower borrower)
    {
        boolean makeRequest = true;
        //  如果一个借书人已经借了这本书，那他就不能再预约这本书了。他必须通过"续借"来延长还书截止日期。
        for(int i=0;i<borrower.getBorrowedBooks().size();i++)
        {
            if(borrower.getBorrowedBooks().get(i).getBook()==this)
            {
                System.out.println("\n" + "你已经借过 《" + title + "》\n" + "请通过\"续借\"来延长还书截止日期。");
                return;                
            }
        }
        
        
        //   如果一个人未借，但已预约过就不让二次预约。
        for (int i = 0; i < holdRequests.size(); i++)
        {
            if ((holdRequests.get(i).getBorrower() == borrower))
            {
                makeRequest = false;    
                break;
            }
        }

        if (makeRequest)
        {
            placeBookOnHold(borrower);
        }
        else
            System.out.println("\n你已预约过此书\n");
    }

    
    // 移除指定的预约请求
    public void serviceHoldRequest(HoldRequest hr)
    {
        HoldRequestOperations.removeHoldRequest();
        hr.getBorrower().removeHoldRequest(hr);
    }

    
        
    // 借出图书
    public void issueBook(Borrower borrower, Staff staff)
    {        
        // 清理过期的预约请求
        Date today = new Date();        
        
        for (int i = holdRequests.size() - 1; i >= 0; i--)
        {
            HoldRequest hr = holdRequests.get(i);

            long days = ChronoUnit.DAYS.between(hr.getRequestDate().toInstant(), today.toInstant());

            if(days > Library.getInstance().getHoldRequestExpiry())
            {
                HoldRequestOperations.removeHoldRequestAt(i);
                hr.getBorrower().removeHoldRequest(hr);
            }
        }

        if (isIssued)
        {
            System.out.println("\n这本书:  " + title + " 已经被借走了。");
            System.out.println("您要预约这本书吗 (y/n)");
             
            Scanner sc = new Scanner(System.in);
            String choice = sc.next();
            
            if (choice.equals("y"))
            {                
                makeHoldRequest(borrower);
            }
        }
        
        else
        {               
            if (!holdRequests.isEmpty())
            {
                boolean hasRequest = false;
                
                for (int i = 0; i < holdRequests.size() && !hasRequest; i++)
                {
                    if (holdRequests.get(i).getBorrower() == borrower) {
                        hasRequest = true;
                        break;
                    }
                        
                }
                
                if (hasRequest)
                {
                    //If this particular borrower has the earliest request for this book
                    if (holdRequests.get(0).getBorrower() == borrower)
                        serviceHoldRequest(holdRequests.get(0));

                    else
                    {
                        System.out.println("\nSorry some other users have requested for this book earlier than you. So you have to wait until their hold requests are processed.");
                        return;
                    }
                }
                else
                {
                    System.out.println("\nSome users have already placed this book on request and you haven't, so the book can't be issued to you.");
                    
                    System.out.println("Would you like to place the book on hold? (y/n)");

                    Scanner sc = new Scanner(System.in);
                    String choice = sc.next();
                    
                    if (choice.equals("y"))
                    {
                        makeHoldRequest(borrower); 
                    }                    
                    
                    return;
                }               
            }
                        
            //If there are no hold requests for this book, then simply issue the book.            
            setIssuedStatus(true);
            
            Loan iHistory = new Loan(borrower,this,staff,null,new Date(),null,false);
            
            Library.getInstance().addLoan(iHistory);
            borrower.addBorrowedBook(iHistory);
                                    
            System.out.println("\nThe book " + title + " is successfully issued to " + borrower.getName() + ".");
            System.out.println("\nIssued by: " + staff.getName());            
        }
    }
        
        
    // Returning a Book
    public void returnBook(Borrower borrower, Loan l, Staff staff)
    {
        l.getBook().setIssuedStatus(false);        
        l.setReturnedDate(new Date());
        l.setReceiver(staff);        
        
        borrower.removeBorrowedBook(l);
        
        l.payFine();
        
        System.out.println("\nThe book " + l.getBook().getTitle() + " is successfully returned by " + borrower.getName() + ".");
        System.out.println("\nReceived by: " + staff.getName());            
    }
    
}   // Book Class Closed