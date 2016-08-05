package pyl;

import java.math.BigDecimal;

public class EngineChildFirstCL
{
    public static void main(String[] args) {
        BigDecimal bd = new BigDecimal(10);
        
        if(bd.intValue() == 0) 
        {
            throw new RuntimeException("Value always 0");
        }
    }
}
