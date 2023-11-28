// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.15;

contract GetCodeHashes {

    function getCodeHashes(address[] memory addresses) public view returns (bytes32) {
        bytes32[] memory hashes = new bytes32[](addresses.length);
        for (uint i = 0; i < addresses.length; i++) {
            hashes[i] = addresses[i].codehash;
        }
        bytes memory data = abi.encode(hashes);
        return (keccak256(data));
    }

}
