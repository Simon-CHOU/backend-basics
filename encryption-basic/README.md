# Java 加密

参考：
- Core Java 10th Ch 9.5 encryption

## 对称加密
DES 过时，用 AES。
区块填充，如果不能被正处，后面补0（padding）,但这造成解密结果多了0，
所以需要更好的padding模式：Public Key Cryptography Standard (PKCS) ，通过规则（），解密后自动丢弃填充部分。

## 密钥生成
为了加密，你需要生成一个密钥
每个密码都有不同的密钥格式，你需要确保密钥生成是随机的。
> 1. 新建 KeyGenerator 对象
> 2. 使用随机源初始化 generator（安全随机发生器的初始化是很耗时的）
> 3. 经 generateKey() 方法获取密钥

Random 不够随机。如果攻击者知道钥匙的发行日期（通常可以从信息日期或证书到期日期推断出来），那么生成该日的所有可能的种子是很容易的事情。
所以使用 SecureRandom 就好得多。
当然，组好是硬件生成——做到这一点的最佳方法是从一个硬件设备（如白噪声发生器）中获得随机输入。
让用户乱敲键盘也以。
```dtd
    SecureRandom secrand = new SecureRandom();
    byte[] b = new byte[20];
    // fill with truly random bits
    secrand.setSeed(b);
```

Demo AESTest

```shell
# 生成对称密钥，并将其序列化到给定的文件中
java aes.AESTest -genkey secret.key
# 使用密钥加密文本文件
java aes.AESTest -encrypt plaintextFile encryptedFile secret.key
# 使用密钥解密密文
java aes.AESTest -decrypt encryptedFile decryptedFile secret.key
```


疑问：
如何使用CLI编译 AESTest和Util
如何使用IDEA编译 AESTest和Util，并通过CLI执行 .class
