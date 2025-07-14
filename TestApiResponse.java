import com.sportvenue.common.model.ApiResponse;
import java.util.HashMap;
import java.util.Map;

public class TestApiResponse {
    public static void main(String[] args) {
        // 测试ApiResponse的基本功能
        ApiResponse<String> response1 = ApiResponse.success("test data");
        System.out.println("Success response: " + response1);
        System.out.println("isSuccess: " + response1.isSuccess());
        System.out.println("isError: " + response1.isError());
        
        ApiResponse<String> response2 = ApiResponse.error("test error");
        System.out.println("Error response: " + response2);
        System.out.println("isSuccess: " + response2.isSuccess());
        System.out.println("isError: " + response2.isError());
        
        // 测试Map类型的响应
        Map<String, Object> data = new HashMap<>();
        data.put("user", "admin");
        data.put("token", "test-token");
        
        ApiResponse<Map<String, Object>> response3 = ApiResponse.success(data);
        System.out.println("Map response: " + response3);
        System.out.println("isSuccess: " + response3.isSuccess());
        
        if (response3.isSuccess()) {
            Map<String, Object> result = response3.getData();
            System.out.println("Data: " + result);
        }
    }
} 