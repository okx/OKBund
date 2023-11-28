// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.12;


contract OpL1Block {

    address public singleton = 0x4200000000000000000000000000000000000015;

    function getAllInfo() external view returns (uint64, uint64, uint256, bytes32, uint64, bytes32, uint256, uint256){
        IOpL1Block s = IOpL1Block(singleton);
        return (s.number(), s.timestamp(), s.basefee(), s.hash(), s.sequenceNumber(), s.batcherHash(), s.l1FeeOverhead(), s.l1FeeScalar());
    }
}

interface IOpL1Block {

    /// @notice The latest L1 block number known by the L2 system.
    function number() external view returns (uint64);

    /// @notice The latest L1 timestamp known by the L2 system.
    function timestamp() external view returns (uint64);

    /// @notice The latest L1 basefee.
    function basefee() external view returns (uint256);

    /// @notice The latest L1 blockhash.
    function hash() external view returns (bytes32);

    /// @notice The number of L2 blocks in the same epoch.
    function sequenceNumber() external view returns (uint64);

    /// @notice The versioned hash to authenticate the batcher by.
    function batcherHash() external view returns (bytes32);

    /// @notice The overhead value applied to the L1 portion of the transaction fee.
    function l1FeeOverhead() external view returns (uint256);

    /// @notice The scalar value applied to the L1 portion of the transaction fee.
    function l1FeeScalar() external view returns (uint256);

}
