# 本地文件推荐算法设计

本项目要实现一个本地文件推荐列表，核心目标是预测用户此刻最可能打开哪个文件。

## 核心因素

推荐算法只重点考虑两个因素：

1. 时间间隔规律  
   例如用户大约每 5 天打开一次文件 X，那么当距离上次打开接近 5 天时，文件 X 的推荐分数应升高。

2. 打开后的后继关系  
   例如用户打开文件 A 后，经常接着打开文件 B，那么当用户刚打开 A 时，B 应进入推荐列表前列。

## 推荐模型

每个候选文件计算一个 finalScore：

```text
finalScore(file) =
  baseIntervalWeight * intervalScore(file)
+ baseTransitionWeight * transitionScore(previousFile, file)
+ baseRecencyWeight * recencyScore(file)
+ learnedIntervalWeight * intervalScore(file)
+ learnedTransitionWeight * transitionScore(previousFile, file)
+ learnedRecencyWeight * recencyScore(file)

固定基础权重
基础权重用于保证冷启动时推荐不至于太差。

baseIntervalWeight = 1.2
baseTransitionWeight = 1.5
baseRecencyWeight = 0.4

动态学习权重

learnedIntervalWeight、learnedTransitionWeight、learnedRecencyWeight 根据用户反馈在线更新。
用户打开推荐列表中的某个文件时：
	•	被打开的文件是正样本
	•	推荐了但没打开的文件是弱负样本
	•	正样本学习率应大于负样本学习率
	•	全局权重学习要慢，避免被偶然行为带偏
建议
positiveLearningRate = 0.05
negativeLearningRate = 0.01


时间间隔规律

每个文件保存：
fileId
lastOpenTime
estimatedPeriod
openCount

用户再次打开文件时，更新 estimatedPeriod：
estimatedPeriod = oldPeriod * 0.8 + latestInterval * 0.2

intervalScore 可使用：
intervalScore = exp(-abs(currentGap - estimatedPeriod) / tolerance)

后继关系

保存文件打开转移次数：
transitionCount[A][B] = count
表示用户打开 A 后接着打开 B 的次数。

transitionScore：
transitionScore(A, B) = count(A -> B) / totalTransitionsFromA

学习速度原则
	•	后继关系可以学得快
	•	文件周期中速学习
	•	全局权重慢速学习
不要让模型因为一次偶然打开就大幅改变推荐结果。

推荐流程
	1.	记录每次文件打开日志
	2.	根据 previousFile 找后继候选文件
	3.	根据周期规律找周期候选文件
	4.	加入少量最近打开文件
	5.	对候选文件计算 intervalScore、transitionScore、recencyScore
	6.	计算 finalScore
	7.	按 finalScore 降序返回 Top 10
	8.	用户打开文件后，更新周期、转移表、动态权重
