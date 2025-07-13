import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String storedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        String inputPassword = "123456";
        
        System.out.println("存储的密码: " + storedPassword);
        System.out.println("输入的密码: " + inputPassword);
        
        boolean matches = encoder.matches(inputPassword, storedPassword);
        System.out.println("匹配结果: " + matches);
        
        // 测试重新编码
        String newEncoded = encoder.encode("123456");
        System.out.println("重新编码123456: " + newEncoded);
        boolean newMatches = encoder.matches("123456", newEncoded);
        System.out.println("新编码匹配结果: " + newMatches);
    }
} 