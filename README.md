# WeirdChat 🥴

WeirdChat is a FabricMC mod that adds Emoji support to the Minecraft chat. Instead of completely rewriting the text
renderer, Discord emoji shortcodes (e.g. `:sparkles:` or `:sweat_smile:`) are replaced with their
[Twemoji](https://twemoji.twitter.com) image. Additionally, autocomplete support for the shortcodes is provided
via [MoreChatSuggestions](https://github.com/SarahIsWeird/MoreChatSuggestions).

The shortcodes are only replaced in chat messages. While it would be possible to do this elsewhere too, a lot of weird
bugs and graphical glitches would result. If there is demand, I might add support for this everywhere.

You can use a resource pack to change the emoji images. If you intend to do so, please
[create an issue](https://github.com/SarahIsWeird/WeirdChat/issues) for further assistance.

## Dependencies

- fabric-api >= `0.64.0+1.19.2`
- fabric-language-kotlin >= `1.8.5+kotlin.1.7.20`
- MoreChatSuggestions >= `1.0`

## Planned features

- Fitzpatrick (skin tone) modifier support
- API to add custom emotes


## Acknowledgements

- Twitter for making their [Twemoji](https://twemoji.twitter.com) images free to use
- The [FabricMC Discord](https://discord.gg/v6v4pMv) for helping with mixins

## Including WeirdChat in your mod

At the moment this isn't very useful, but in the future you will be able to register your own emoji via an API.

Add this to your repositories:

```groovy
// build.gradle
maven {
    url "https://maven.sarahisweird.com/releases"
}

// build.gradle.kts
maven("https://maven.sarahisweird.com/releases")
```

And this to your dependencies:

```groovy
// build.gradle
modImplementation("com.sarahisweird:WeirdChat:1.0")

// build.gradle.kts
modImplementation("com.sarahisweird", "WeirdChat", "1.0")
```

## Building WeirdChat from source

Firstly ask yourself: do you really want to do this? Okay, good. Prepare yourself.

### Dependencies

- Java 17
- Node.js >= v18.11.0 (for generating the textures and metadata)

```bash
git clone https://github.com/SarahIsWeird/WeirdChat.git --recurse-submodules
```

**IMPORTANT!** The repository is huge (~1GB)! This is because of the Twemoji submodule. I'm not really sure why, as the
entire `twemoji` folder is only about 12MB. It seems like git is downloading the entire history of the repository, which
is absolutely enormous. 🤷🏻‍♀️

### Generating the assets

This has to be done before starting Minecraft. You can build the mod in Gradle before this step, but if you don't run
the asset generator, it no worky.

```bash
cd WeirdChat/gen
npm install
npm start -- --generate-textures
```

At this point you can start Minecraft!

#### Options

If you specify any option, please don't forget to add `--` before them, or they won't be applied! (See example above)

- `--generate-textures`
  - Generate the emoji texture atlases. If you don't want to do this, for example if you only want to fix something
    in the metadata generation, you can omit this. In that case, only the metadata json is generated.
- `--debug`
  - By default, the resulting metadata json will be minified. Specifying this flag will add indentation.

# License

WeirdChat is released under the WTFPL. For more information, please see the [LICENSE](LICENSE) file.

WeirdChat uses [Twemoji by Twitter](https://twemoji.twitter.com), with the used graphics licensed under the CC-BY 4.0
license. For more information, please see the
[LICENSE-GRAPHICS file in their repository](https://github.com/twitter/twemoji/blob/master/LICENSE-GRAPHICS).

The asset generator uses source code from [kordx.emoji](https://github.com/kordlib/kordx.emoji), licensed under the MIT
license. For more information, please see the
[LICENSE file in their repository](https://github.com/kordlib/kord/blob/0.8.x/LICENSE).
