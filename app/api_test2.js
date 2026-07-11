const https = require('https');
const querystring = require('querystring');

const postData = querystring.stringify({
  hash: '27dbb28ea03fd64ae84f717f6dfac59c',
  r: 'https://javrider.id/fc2-ppv-4401493/'
});

const options = {
  hostname: 'javplayers.com',
  port: 443,
  path: '/player/index.php?data=27dbb28ea03fd64ae84f717f6dfac59c&do=getVideo',
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Content-Length': Buffer.byteLength(postData),
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
    'X-Requested-With': 'XMLHttpRequest'
  }
};

const req = https.request(options, (res) => {
  let chunks = [];
  res.on('data', (d) => {
    chunks.push(d);
  });
  res.on('end', () => {
    console.log("RESPONSE HTTP " + res.statusCode);
    console.log(Buffer.concat(chunks).toString());
  });
});

req.on('error', (e) => {
  console.error(e);
});

req.write(postData);
req.end();
