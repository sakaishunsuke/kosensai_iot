// Iot機器のデータをモバイルバックエンドにうｐするためのjs
var NCMB = require("ncmb");
var ncmb = new NCMB("4dd8d0b46a5b342c8c91d16326eeefdc32279ddbd8d26fa227ad062863318987",
					"a869d2c96f84bb2b24a3a45df8f7731b6f365b6390bf6addff606f643747012b");
function main() {
	// クラスのTestClassを作成
	var TestClass = ncmb.DataStore("TestClass");

	// データストアへの登録
	var testClass = new TestClass();
	testClass.set("message", "Hello, NCMB!");
	testClass.save()
         .then(function(){
            // 保存に成功した場合の処理
          })
         .catch(function(e){
            // 保存に失敗した場合の処理
          });
		 
}
