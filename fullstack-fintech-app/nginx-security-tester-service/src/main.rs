mod test;

use test::security_tester::SecurityTester;

fn main() {
    println!("\n🔐 Spring Boot Security Tester");
    println!("=============================");
    
    let args: Vec<String> = std::env::args().collect();
    let base_url = if args.len() > 1 {
        args[1].clone()
    } else {
        "http://localhost:8292".to_string() 
    };
   
    println!("🌐 Testing Spring Boot Application at: {}", base_url);
    
    let mut tester = SecurityTester::new(&base_url);
    tester.test_all();
}