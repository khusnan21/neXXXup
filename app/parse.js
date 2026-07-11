const fs = require('fs');
const content = fs.readFileSync('app/test_output.txt', 'utf-8');
const regex = /(eval\(function\(p,a,c,k,e,d\).*?\.split\('\|'\).*?\)\))/;
const m = regex.exec(content);
if(m) {
    let raw = m[1];
    let toRun = raw.replace(/^eval/, 'console.log');
    fs.writeFileSync('unpack.js', toRun);
} else {
    console.log("Not found");
}
