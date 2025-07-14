import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + encodedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 测试数据库中的密码
        String dbPassword = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        boolean dbMatches = encoder.matches(password, dbPassword);
        System.out.println("数据库密码验证结果: " + dbMatches);
    }
} 