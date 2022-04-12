
## Wrong Cofnig 1
spring:
  profiles:
    active: @profileActive@
```log
org.yaml.snakeyaml.scanner.ScannerException: while scanning for the next token
found character '@' that cannot start any token. (Do not use @ for indentation)
in 'reader', line 3, column 13:
active: @profileActive@
```

## Wrong Config 2
spring:
  profiles:
    active: "@profileActive@"
```log
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'displayCountCommand': Injection of autowired dependencies failed; nested exception is java.lang.IllegalArgumentException: Could not resolve placeholder 'count' in value "${count}"

```

## Wrong Config 3

spring:
  profiles:
    active: "@profile.active@"


## Conclusion

use snakeyaml

spring:
  profiles:
    active: @profileActive@

spring:
  profiles:
    active: '@profileActive@'

both ok


## Reference
https://blog.csdn.net/qq_35981283/article/details/78635273
https://blog.csdn.net/fengxing_2/article/details/94620478