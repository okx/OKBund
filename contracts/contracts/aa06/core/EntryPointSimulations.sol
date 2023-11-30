// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.12;

/* solhint-disable avoid-low-level-calls */
/* solhint-disable no-inline-assembly */

import "../interfaces/IEntryPointSimulations.sol";
import "../core/EntryPoint.sol";

/*
 * This contract inherits the EntryPoint and extends it with the view-only methods that are executed by
 * the bundler in order to check UserOperation validity and estimate its gas consumption.
 * This contract should never be deployed on-chain and is only used as a parameter for the "eth_call" request.
 */
contract EntryPointSimulations is EntryPoint, IEntryPointSimulations {
    // solhint-disable-next-line var-name-mixedcase
    AggregatorStakeInfo private NOT_AGGREGATED = AggregatorStakeInfo(address(0), StakeInfo(0, 0));

    bool internal  execSuccess;
    uint256 internal userOpActualGasUsed;
    uint256 internal userOpPostOpGas;
    bytes internal execErrMsg;

    /**
     * simulation contract should not be deployed, and specifically, accounts should not trust
     * it as entrypoint, since the simulation functions don't check the signatures
     */
    constructor() {
        require(block.number < 100, "should not be deployed");
    }

    /// @inheritdoc IEntryPointSimulations
    function simulateHandleOp(
        UserOperation calldata op,
        address target,
        bytes calldata targetCallData
    )
    external nonReentrant {
        uint256 estimateGasUsed = 0;
        UserOpInfo memory opInfo;
        _simulationOnlyValidations(op);
        (
            uint256 validationData,
            uint256 paymasterValidationData
        ) = _validatePrepayment(0, op, opInfo);
        ValidationData memory data = _intersectTimeRange(
            validationData,
            paymasterValidationData
        );

        numberMarker();
        uint256 paid = _executeUserOp(0, op, opInfo);
        numberMarker();
        bool targetSuccess;
        bytes memory targetResult;
        if (target != address(0)) {
            (targetSuccess, targetResult) = target.call(targetCallData);
        }

        uint256 actualGasUsed = userOpActualGasUsed + estimateGasUsed;
        uint256 preVerificationGas = op.preVerificationGas;
        uint256 userOpVerificationGasUsed = opInfo.preOpGas - preVerificationGas + estimateGasUsed;
        uint256 estimateGasCost = estimateGasUsed * getUserOpGasPrice(opInfo.mUserOp);
        paid = paid + estimateGasCost;
        revert SimulateHandleOpResult(
            data.validAfter,
            data.validUntil,
            data.aggregator,
            preVerificationGas,
            userOpVerificationGasUsed,
            execSuccess,
            execErrMsg,
            actualGasUsed,
            userOpPostOpGas,
            paid,
            targetSuccess,
            targetResult
        );
    }

    function _simulationOnlyValidations(
        UserOperation calldata userOp
    )
    internal
    view
    {
        try
        this._validateSenderAndPaymaster(
            userOp.initCode,
            userOp.sender,
            userOp.paymasterAndData
        )
        // solhint-disable-next-line no-empty-blocks
        {} catch Error(string memory revertReason) {
            if (bytes(revertReason).length != 0) {
                revert FailedOp(0, revertReason);
            }
        }
    }

    /**
     * Called only during simulation.
     * This function always reverts to prevent warm/cold storage differentiation in simulation vs execution.
     * @param initCode         - The smart account constructor code.
     * @param sender           - The sender address.
     * @param paymasterAndData - The paymaster address followed by the token address to use.
     */
    function _validateSenderAndPaymaster(
        bytes calldata initCode,
        address sender,
        bytes calldata paymasterAndData
    ) external view {
        if (initCode.length == 0 && sender.code.length == 0) {
            // it would revert anyway. but give a meaningful message
            revert("AA20 account not deployed");
        }
        if (paymasterAndData.length >= 20) {
            address paymaster = address(bytes20(paymasterAndData[0 : 20]));
            if (paymaster.code.length == 0) {
                // It would revert anyway. but give a meaningful message.
                revert("AA30 paymaster not deployed");
            }
        }
        // always revert
        revert("");
    }

    //make sure depositTo cost is more than normal EntryPoint's cost.
    // empiric test showed that without this wrapper, simulation depositTo costs less..
    function depositTo(address account) public override(IStakeManager, StakeManager) payable {
        uint x;
        assembly {
        //some silly code to waste ~200 gas
            x := exp(mload(0), 100)
        }
        if (x == 123) {
            return;
        }
        StakeManager.depositTo(account);
    }

    function _postOpGas(uint256 postOpGas) internal override {
        userOpPostOpGas = postOpGas;
    }

    // internally called by simulateHandleOp. instead of emit event
    // (which we can't see - its a static call),
    // we save into global storage
    function _emitUserOperationEvent(
        UserOpInfo memory opInfo,
        bool success,
        uint256 actualGasCost,
        uint256 actualGas
    ) internal override {

        userOpActualGasUsed = actualGas;
        execSuccess = success;

        MemoryUserOp memory mUserOp = opInfo.mUserOp;
        emit UserOperationEvent(
            opInfo.userOpHash,
            mUserOp.sender,
            mUserOp.paymaster,
            mUserOp.nonce,
            success,
            actualGasCost,
            actualGas
        );
    }

    function _emitUserOperationRevertReason(UserOpInfo memory opInfo, bytes memory result) internal override {
        execErrMsg = result;
        MemoryUserOp memory mUserOp = opInfo.mUserOp;
        emit UserOperationRevertReason(
            opInfo.userOpHash,
            mUserOp.sender,
            mUserOp.nonce,
            result
        );
    }
}
