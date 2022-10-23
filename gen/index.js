const Jimp = require('jimp');
const { createWriteStream } = require('fs');
const { readdir, readFile, writeFile } = require('fs/promises');
const path = require('path');
const https = require("https");
const { convert: { toCodePoint } } = require('twemoji');
const { parseArgs } = require('node:util');

const options = {
    'generate-textures': {
        type: 'boolean',
        short: 't',
        default: false,
    },
    debug: {
        type: 'boolean',
        short: 'd',
        default: false,
    },
};

const { values: args } = parseArgs({ options });
const { 'generate-textures': generateTextures, debug } = args;

const emojiListKtUrl = 'https://raw.githubusercontent.com/kordlib/kordx.emoji/master/src/main/kotlin/dev/kord/x/emoji/EmojiList.kt';

const pathToEmoji = path.join(__dirname, '../twemoji/assets/72x72');
const pathToEmojiListKt = path.join(__dirname, 'EmojiList.kt');

const outputPath = path.join(__dirname, '../src/main/resources/assets/weirdchat/textures/emoji')
const dataOutputPath = path.join(__dirname, '../src/main/resources/data/weirdchat/emoji.json')

const EMOJI_SIZE = 72;
const ROWS = 16;
const COLUMNS = 16;

const PAGE_WIDTH = EMOJI_SIZE * COLUMNS;
const PAGE_HEIGHT = EMOJI_SIZE * ROWS;

const main = async () => {
    await downloadFile(emojiListKtUrl, pathToEmojiListKt);

    const data = await generateTransformedEmojiData();
    const annotatedData = await generateAssets(data);

    const jsonString = JSON.stringify(annotatedData, null, debug ? 4 : 0)
    await writeFile(dataOutputPath, jsonString);

    console.log('Done.');
};

const generateTransformedEmojiData = async () => {
    const dataRegex = /(?<unicode>"(\\u[0-9a-f]{4})+") to `(?<name>\w+)`/gim;
    const data = await readFile(pathToEmojiListKt, { encoding: 'utf-8' });

    let newData = [];

    let match;

    while (match = dataRegex.exec(data)) {
        const { unicode, name } = match.groups;

        const unescaped = JSON.parse(unicode);
        const codePoint = toCodePoint(unescaped);

        let shortCode = name;

        shortCode = shortCode.replaceAll(/[a-zA-Z]\d[a-zA-Z]/g, str => `${str[0]}_${str[1]}_${str[2]}`);
        shortCode = shortCode.replaceAll(/[a-z][A-Z]/g, str => str[0] + '_' + str[1].toLowerCase());
        shortCode = shortCode.replaceAll(/_[A-Z]/g, str => `_` + str[1].toLowerCase());

        newData.push({
            alias: shortCode,
            codePoint: codePoint,
        });
    }

    return newData;
};

const downloadFile = async (url, savePath) => new Promise((resolve, reject) => {
    const req = https.get(url, (res) => {
        const writeStream = createWriteStream(savePath);

        res.pipe(writeStream);

        writeStream.on("finish", () => {
            writeStream.close();
            req.end();

            resolve();
        });

        writeStream.on("error", reject);
    });

    req.on("error", reject);
});

const generateAssets = async (data) => {
    const allFiles = await readdir(pathToEmoji);
    const files = allFiles.filter(fileName => {
        const baseName = path.basename(fileName, '.png');

        return data.find(el => el.codePoint === baseName) !== undefined;
    });

    const pages = divideArray(files, COLUMNS * ROWS).map(page => divideArray(page, ROWS));
    const pageCount = pages.length;

    let newData = [];

    for (const page of pages) {
        const pageI = pages.indexOf(page);

        newData.push.apply(newData, await generatePage(page, pageI, pageCount, data));
    }

    return newData;
};

const generatePage = async (page, pageI, pageCount, data) => {
    let newData = [];
    let image;

    if (generateTextures) {
        image = await new Jimp(PAGE_WIDTH, PAGE_HEIGHT);
    }

    for (const row of page) {
        const rowI = page.indexOf(row);

        newData.push.apply(newData, await generateRow(row, rowI, pageI, image, data));
    }

    if (generateTextures) {
        const pagePath = path.join(outputPath, `emoji_page_${pageI}.png`);
        image.write(pagePath, writeError => {
            if (writeError) {
                console.error(`Failed to write page ${pageI + 1}: ${writeError}`);
                return;
            }

            console.info(`Written page ${pageI + 1}/${pageCount}.`);
        });
    }

    return newData;
};

const generateRow = async (row, rowI, pageI, image, data) => {
    let newData = [];

    for (const emojiFile of row) {
        const colI = row.indexOf(emojiFile);
        const baseName = emojiFile.substring(0, emojiFile.length - '.png'.length);

        const emojiInfo = data.find(el => el.codePoint === baseName);

        newData.push({
            alias: `:${emojiInfo.alias}:`,
            page: pageI,
            row: rowI,
            column: colI,
        });

        if (generateTextures) {
            const emoji = await Jimp.read(path.join(pathToEmoji, emojiFile));

            image.blit(emoji, colI * EMOJI_SIZE, rowI * EMOJI_SIZE)
        }
    }

    return newData;
};

const divideArray = (arr, count) => {
    const newArray = [];

    while (arr.length > count) {
        newArray.push(arr.splice(0, count));
    }

    if (arr.length > 0) {
        newArray.push(arr);
    }

    return newArray;
};

main().then();
