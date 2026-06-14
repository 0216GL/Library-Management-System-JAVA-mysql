
package LMS;

import java.io.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Book {

    private final String isbn;    //ISBN是图书的唯一标识符（国际标准书号）
    private String title;         // 书名
    private String subject;       // 类目
    private String author;        // 作者
    private boolean isIssued;     // 是否正在外借中
    private final ArrayList<HoldRequest> holdRequests;  // 本书的预约队列
  
    public Book(String isbn, String t, String s, String a, boolean issued)
    {
        this.isbn = isbn;
        title = t;
        subject = s;
        author = a;
        isIssued = issued;
        this.holdRequests = new ArrayList<>();
    }


    // 打印所有图书预约列表
    public void printHoldRequests()
    {
        if (!holdRequests.isEmpty())
        { 
            System.out.println("\n《" + title + "》的预约列表（" + holdRequests.size() + "/3）: ");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");            
            System.out.println("序号\t\t预约者\t\t\t预约日期");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");
            
            for (int i = 0; i < holdRequests.size(); i++)
            {                      
                System.out.print((i+1) + "\t\t");
                holdRequests.get(i).print();
            }
        }
        else
            System.out.println("\n《" + title + "》目前没有预约。");
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

        holdRequests.add(hr);
        bor.addHoldRequest(hr);
        
        System.out.println("\n图书 " + title + " 被预约成功。   预约人 :  " + bor.getName() + ".\n");
    }
    
    

    //  图书预约/保留请求
    public void makeHoldRequest(Borrower borrower)
    {
        // 检查预约数量是否已达上限
        if (holdRequests.size() >= 3)
        {
            System.out.println("\n抱歉，《" + title + "》的预约已满（最多3人）。");
            return;
        }
        
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
        for (HoldRequest holdRequest : holdRequests) {
            if ((holdRequest.getBorrower() == borrower)) {
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
        holdRequests.remove(hr);
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
                holdRequests.remove(i);
                hr.getBorrower().removeHoldRequest(hr);
            }
        }

        // 始终显示当前预约队列
        printHoldRequests();

        if (isIssued)
        {
            System.out.println("\n这本书:  " + title + " 已经被借走了。");
            
            // 检查是否可以预约
            if (holdRequests.size() < 3)
            {
                System.out.println("\n当前预约人数：" + holdRequests.size() + "/3");
                System.out.println("您要预约这本书吗？(y/n)");
                 
                Scanner sc = new Scanner(System.in);
                String choice = sc.next();
                
                if (choice.equals("y"))
                {                
                    makeHoldRequest(borrower);
                }
            }
            else
            {
                System.out.println("\n预约已满，无法继续预约。");
            }
        }
        
        else
        {               
            if (!holdRequests.isEmpty())
            {
                // ✅ 修复：检查当前借阅者是否在预约队列中
                int borrowerPosition = -1;
                for (int i = 0; i < holdRequests.size(); i++) {
                    if (holdRequests.get(i).getBorrower() == borrower) {
                        borrowerPosition = i;
                        break;
                    }
                }
                
                if (borrowerPosition != -1)
                { 
                    // ✅ 修复：只有当借阅者是第一个预约者时才允许借书
                    if (borrowerPosition == 0)
                    {
                        serviceHoldRequest(holdRequests.get(0));
                    }
                    else
                    {
                        System.out.println("\n抱歉，其他用户已经提前预约了此书，所以您暂时不能借阅此书。");
                        System.out.println("您在预约队列中的位置：" + (borrowerPosition + 1));
                        return;
                    }
                }
                else
                {
                    System.out.println("\n已有用户预约了这本书，而你没有预约，所以不能借给你。");
                    
                    // 检查是否可以预约
                    if (holdRequests.size() < 3)
                    {
                        System.out.println("\n当前预约人数：" + holdRequests.size() + "/3");
                        System.out.println("您要预约这本书吗？(y/n)");

                        Scanner sc = new Scanner(System.in);
                        String choice = sc.next();
                        
                        if (choice.equals("y"))
                        {
                            makeHoldRequest(borrower); 
                        }
                    }
                    else
                    {
                        System.out.println("\n预约已满，无法继续预约。");
                    }
                    
                    return;
                }               
            }
            
            //如果没有预约请求，那么就直接借出
                    
            setIssuedStatus(true);
            
            Loan iHistory = new Loan(borrower,this,staff,null,new Date(),null,false);
            
            Library.getInstance().addLoan(iHistory);
            borrower.addBorrowedBook(iHistory);
                                    
            System.out.println("\n这本书 " + title + " 被借出去了。\n借书人 : " + borrower.getName() + ".");
            System.out.println("\n经办人: " + staff.getName());            
        }
    }
        
        
    // 还书
    public void returnBook(Borrower borrower, Loan l, Staff staff)
    {
        l.getBook().setIssuedStatus(false);        
        l.setReturnedDate(new Date());
        l.setReceiver(staff);        
        
        borrower.removeBorrowedBook(l);
        
        l.payFine();
        
        System.out.println("\n图书 " + l.getBook().getTitle() + " 还书成功。  还书人 :  " + borrower.getName() + ".");
        System.out.println("\n办理员工: " + staff.getName());
    }
    
}   // Book Class Closed