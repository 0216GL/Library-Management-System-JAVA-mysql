
package LMS;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Loan 
{
    private Borrower borrower;      
    private Book book;
    
    private Staff issuer;
    private Date issuedDate;
    
    private Date dateReturned;
    private Staff receiver;
    
    private boolean finePaid;
       
    public Loan(Borrower bor, Book b, Staff i, Staff r, Date iDate, Date rDate, boolean fPaid)
    {
        borrower = bor;
        book = b;
        issuer = i;
        receiver = r;
        issuedDate = iDate;
        dateReturned = rDate;
        finePaid = fPaid;
    }
    
    /*----- Getter FUNCs.------------*/
    
    public Book getBook()
    {
        return book;
    }
    
    public Staff getIssuer()
    {
        return issuer;
    }
    
    public Staff getReceiver()
    {
        return receiver;
    }
    
    public Date getIssuedDate()
    {
        return issuedDate;
    } 

    public Date getReturnDate()
    {
        return dateReturned;
    }
    
    public Borrower getBorrower()
    {
        return borrower;
    }
    
    public boolean getFineStatus()
    {
        return finePaid;
    }
    /*---------------------------------------------*/
    
    
    /*----------Setter FUNCs.---------------------*/
    public void setReturnedDate(Date dReturned)
    {
        dateReturned = dReturned;
    }
    
    public void setFineStatus(boolean fStatus)
    {
        finePaid = fStatus;
    }    
    
    public void setReceiver(Staff r)
    {
        receiver = r;
    }
    /*-------------------------------------------*/
    



    //计算罚款
    public double computeFine1()
    {

        //-----------Computing Fine-----------        
        double totalFine = 0;
        
        if (!finePaid)
        {    
            Date iDate = issuedDate;
            Date rDate = new Date();                

            long days =  ChronoUnit.DAYS.between( iDate.toInstant(),rDate.toInstant());

            days = days - Library.getInstance().book_return_deadline;

            if(days>0)
                totalFine = days * Library.getInstance().per_day_fine;
            else
                totalFine=0;
        }
        return totalFine;
    }
    
    
    public void payFine()
    {
        //-----------Computing Fine-----------//
        
        double totalFine = computeFine1();
                
        if (totalFine > 0)
        {
            System.out.println("\n超出规定还书时间，应缴罚款 : " + totalFine);
            
            Scanner input = new Scanner(System.in); 
            
            String choice = input.next();
            
            if(choice.equals("y") || choice.equals("Y"))
                finePaid = true; 
            
            if(choice.equals("n") || choice.equals("N"))
                finePaid = false; 
        }
        else
        {
            System.out.println("\n  还书成功  ");
            finePaid = true;
        }        
    }


    // Extending issued Date 
    /**
     * 续借图书
     * 只有在无人预约的情况下才允许续借
     * 
     * @param iDate 新的借阅日期（续借日期）
     */
    public void renewIssuedBook(Date iDate)
    {        
        // 检查该书是否有其他人预约
        ArrayList<HoldRequest> holdRequests = book.getHoldRequests();
        
        if (!holdRequests.isEmpty())
        {
            System.out.println("\n抱歉，《" + book.getTitle() + "》已有其他用户预约。");
            System.out.println("当前预约人数：" + holdRequests.size() + "/3");
            System.out.println("无法续借，请按时归还图书。");
            return;
        }
        
        // 无人预约，允许续借
        issuedDate = iDate;
        
        System.out.println("\n《" + book.getTitle() + "》续借成功！");
        System.out.println("新的借阅日期：" + iDate);
        System.out.println("请在规定时间内归还图书。\n");
    }
    
}
