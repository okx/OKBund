![20231128-144704.webp](static/20231128-144704.webp)

# OKBund

___
A bundler implemented in Java, lightweight, easy to develop, and compliant with the EIP-4337 standard.

## Getting started

___

### Prerequisites:

+ Java version 1.8+
+ Docker

### How to run

+ Modify the configuration file `test.env` to customize the public blockchain used.

```
// In the dev environment, the Entrypoint protocol will be initialized by default.
BUNDLER_ENV=dev

// default chain ID
CHAIN_ID=1337

// Customize RPC link
ETH_RPC_URL=http://eth-node:8545

// support EIP-1559
EIP1559=true

// track transaction traces
SAFE_MODE=true	

// bundler private key
BUNDLER_PRIVATE_KEY=

ETH_RPC_URL=http://eth-node:8545

ENTRYPOINT=0x5ff137d4b0fdcd49dca30c7cf57e578a026d2789
```

+ bundler start

```
./start.sh start
```

## Contact

The best place for the discussion is the dedicated [Telegram group](https://t.me/+YPlndYlAiKdiODY1). \
You can also contact our PM or DEV:

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="16.66%"><a href="https://t.me/honosu"><img src="static/honosu.jpeg" width="320" alt="Nanfeng Jie"/><br /><sub><b>Nanfeng Jie (PM)</b></sub></a></td>
      <td align="center" valign="top" width="16.66%"><a href="https://t.me/MinnerChou"><img src="static/MinnerChou.jpeg" width="320" alt="Kay Zhang"/><br /><sub><b>Kay Zhang (PM)</b></sub></a><br /></td>
      <td align="center" valign="top" width="16.66%"><a href="https://t.me/tx_yukino"><img src="static/tx_yukino.jpeg" width="320" alt="Xin Tian"/><br /><sub><b>Xin Tian (DEV)</b></sub></a><br /></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

## Contributing
Thank you for showing interest in contributing to the project!
<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/txgyy"><img src="https://avatars.githubusercontent.com/u/20449332?v=4" width="200px;" alt="Xin Tian"/><br /><sub><b>Xin Tian</b></sub></a><br /><a href="https://x.com/xin_yukino_web3" title="Communication">💬</a></td>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/Zoffy1001"><img src="https://avatars.githubusercontent.com/u/1066889?v=4" width="200px;" alt="Zoffy Chen"/><br /><sub><b>Zoffy Chen</b></sub></a></td>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/cryptoyin"><img src="https://avatars.githubusercontent.com/u/152254710?v=4" width="200px;" alt="Felix Fan"/><br /><sub><b>Felix Fan</b></sub></a></td>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/tian12138yu"><img src="https://avatars.githubusercontent.com/u/57339550?v=4" width="200px;" alt="Tyler Tian"/><br /><sub><b>Tyler Tian</b></sub></a></td>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/fanweiqiang"><img src="https://avatars.githubusercontent.com/u/10357294?v=4" width="200px;" alt="Bard Fan"/><br /><sub><b>Bard Fan</b></sub></a></td>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/yuequnqin"><img src="https://avatars.githubusercontent.com/u/8374603?v=4" width="200px;" alt="kevin Yue"/><br /><sub><b>kevin Yue</b></sub></a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

# License

Distributed under the GPL-3.0 License. See [LICENSE](./LICENSE) for more information.

## Acknowledgements

+ [Bundler - eth-infinitism](https://github.com/eth-infinitism/bundler)
