/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.mainnet;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.AncestryValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.CalculatedDifficultyValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.ConstantFieldValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.ExtraDataMaxLengthValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.GasLimitRangeAndDeltaValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.GasUsageValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.ProofOfWorkValidationRule;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.TimestampBoundedByFutureParameter;
import tech.pegasys.pantheon.ethereum.mainnet.headervalidationrules.TimestampMoreRecentThanParent;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public final class MainnetBlockHeaderValidator {

  public static final BytesValue DAO_EXTRA_DATA =
      BytesValue.fromHexString("0x64616f2d686172642d666f726b");
  private static final int MIN_GAS_LIMIT = 5000;
  private static final long MAX_GAS_LIMIT = 0x7fffffffffffffffL;
  public static final int TIMESTAMP_TOLERANCE_S = 15;
  public static final int MINIMUM_SECONDS_SINCE_PARENT = 1;

  public static BlockHeaderValidator<Void> create(
      final DifficultyCalculator<Void> difficultyCalculator) {
    return createValidator(difficultyCalculator).build();
  }

  public static BlockHeaderValidator<Void> createDaoValidator(
      final DifficultyCalculator<Void> difficultyCalculator) {
    return createValidator(difficultyCalculator)
        .addRule(
            new ConstantFieldValidationRule<>(
                "extraData", BlockHeader::getExtraData, DAO_EXTRA_DATA))
        .build();
  }

  public static boolean validateHeaderForDaoFork(final BlockHeader header) {
    return header.getExtraData().equals(DAO_EXTRA_DATA);
  }

  static BlockHeaderValidator<Void> createOmmerValidator(
      final DifficultyCalculator<Void> difficultyCalculator) {
    return new BlockHeaderValidator.Builder<Void>()
        .addRule(new CalculatedDifficultyValidationRule<>(difficultyCalculator))
        .addRule(new AncestryValidationRule())
        .addRule(new GasLimitRangeAndDeltaValidationRule(MIN_GAS_LIMIT, MAX_GAS_LIMIT))
        .addRule(new GasUsageValidationRule())
        .addRule(new TimestampMoreRecentThanParent(MINIMUM_SECONDS_SINCE_PARENT))
        .addRule(new ExtraDataMaxLengthValidationRule(BlockHeader.MAX_EXTRA_DATA_BYTES))
        .addRule(new ProofOfWorkValidationRule())
        .build();
  }

  private static BlockHeaderValidator.Builder<Void> createValidator(
      final DifficultyCalculator<Void> difficultyCalculator) {
    return new BlockHeaderValidator.Builder<Void>()
        .addRule(new CalculatedDifficultyValidationRule<>(difficultyCalculator))
        .addRule(new AncestryValidationRule())
        .addRule(new GasLimitRangeAndDeltaValidationRule(MIN_GAS_LIMIT, MAX_GAS_LIMIT))
        .addRule(new GasUsageValidationRule())
        .addRule(new TimestampMoreRecentThanParent(MINIMUM_SECONDS_SINCE_PARENT))
        .addRule(new TimestampBoundedByFutureParameter(TIMESTAMP_TOLERANCE_S))
        .addRule(new ExtraDataMaxLengthValidationRule(BlockHeader.MAX_EXTRA_DATA_BYTES))
        .addRule(new ProofOfWorkValidationRule());
  }
}
