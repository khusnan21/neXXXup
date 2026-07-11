const https = require('https');

https.get('https://javplayers.com/player/assets/scripts.php?v=6', (res) => {
  let chunks = [];
  res.on('data', (d) => {
    chunks.push(d);
  });
  res.on('end', () => {
      console.log(Buffer.concat(chunks).toString());
  })
});
