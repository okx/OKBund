import {HardhatUserConfig} from 'hardhat/config'

const optimizedCompilerSettings = {
    version: '0.8.17',
    settings: {
        optimizer: {enabled: true, runs: 1000000},
        viaIR: true
    }
}

const config: HardhatUserConfig = {
    solidity: {
        compilers: [{
            version: '0.8.15',
            settings: {
                optimizer: {enabled: true, runs: 1000000}
            }
        }],
        overrides: {
            'contracts/aa06/core/EntryPoint.sol': optimizedCompilerSettings,
        }
    }

}

export default config
