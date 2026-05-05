TDD（Test-Driven Development，测试驱动开发）

1. 编写测试用例
2. 编写代码使测试通过
3. 优化代码
4. 重复以上步骤，直到项目完成
5. McCabe（Cyclomatic Complexity）超过20时，需要进行代码重构
6. 使用jacoco进行代码覆盖率分析
8. 代码行覆盖率、分支覆盖率、方法覆盖率达到99.9%以上
9. 提交代码前，需要进行代码格式化lint
10. 遵守 Google Code Style Guide
11. 单元测试全部通过前，不要进行 e2e 测试
12. 使用playwright进行 e2e 测试
