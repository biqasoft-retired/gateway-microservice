`http://www.textfixer.com/tools/remove-line-breaks.php` - remove lines from javascript code (inline)

### Nashorn

 - https://github.com/javadelight/delight-nashorn-sandbox
 - https://habrahabr.ru/post/195870/
 - https://habrahabr.ru/post/195870/
 - http://stackoverflow.com/questions/20793089/secure-nashorn-js-execution
 - http://winterbe.com/posts/2014/04/05/java8-nashorn-tutorial/

### Java code

static String fun1(String name) {
    System.out.format("Hi there from Java, %s", name);
    return "greetings from java";
}

### JavaScript code

var MyJavaClass = Java.type('my.package.MyJavaClass');
var result = MyJavaClass.fun1('John Doe');
print(result);

// Hi there from Java, John Doe
// greetings from java
//////////////////////////////////////////////////////////

var InterruptTest = Java.type('InterruptTest');
var odds = _.filter([1, 2, 3, 4, 5, 6], function(num) {
    return num % 2 == 1
});
odds // 1, 3, 5