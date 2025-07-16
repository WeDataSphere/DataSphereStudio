/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.common.utils

import java.util.function.{BiFunction, Consumer, Predicate, Supplier,Function}

import org.apache.linkis.protocol.util.ImmutablePair

/**
 * java 调用scala时，传入函数到scala的适配器
 * @date 2022-09-14
 * @author enjoyyin
 * @since 0.5.0
 */
object ScalaFunctionAdapter {

  def doConsumer[P](consumer: Consumer[P]): (P) => Unit = p => consumer.accept(p)

  def doPredicate[P](predicate: Predicate[P]): (P) => Boolean = p => predicate.test(p)

  def doSupplier[R](supplier: Supplier[R]): () => R = () => supplier.get()

  def doFunction[P, R](function: Function[P, R]): (P) => R = p => function.apply(p)

  def doBiFunction[P1, P2, R](function: BiFunction[P1, P2, R]): (P1, P2) => R =
    (p1, p2) => function.apply(p1, p2)

  def do3Function[P1, P2, P3, R](function: BiFunction[ImmutablePair[P1, P2], P3, R]): (P1, P2, P3) => R =
    (p1, p2, p3) => function.apply(new ImmutablePair[P1, P2](p1, p2), p3)

  def do4Function[P1, P2, P3, P4, R](function: BiFunction[ImmutablePair[P1, P2], ImmutablePair[P3, P4], R]): (P1, P2, P3, P4) => R =
    (p1, p2, p3, p4) => function.apply(new ImmutablePair[P1, P2](p1, p2), new ImmutablePair[P3, P4](p3, p4))

}
