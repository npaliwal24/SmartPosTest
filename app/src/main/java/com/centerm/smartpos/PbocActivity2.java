package com.centerm.smartpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.centerm.smartpos.aidl.pboc.AidlCheckCardListener;
import com.centerm.smartpos.aidl.pboc.AidlEMVL2;
import com.centerm.smartpos.aidl.pboc.CardInfoData;
import com.centerm.smartpos.aidl.pboc.CardLoadLog;
import com.centerm.smartpos.aidl.pboc.CardTransLog;
import com.centerm.smartpos.aidl.pboc.EmvTransData;
import com.centerm.smartpos.aidl.pboc.PBOCListener;
import com.centerm.smartpos.aidl.pboc.ParcelableTrackData;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.EMVConstant;
import com.centerm.smartpos.util.EMVTAGS;
import com.centerm.smartpos.util.HexUtil;
import com.centerm.smartpos.util.TlvData;
import com.centerm.smartutils.GetVersionCode;

import java.util.Arrays;

/**
 * PbocActivity2
 * 
 * @desrciption： 新PBOC接口
 * @author:Zmz
 * @date:20160218
 */

public class PbocActivity2 extends BaseActivity {
	public final static byte SERACHE_CARD = 0x00;
	public final static byte READ_CARDNO = 0x01;
	public final static byte READ_OFFILINE_BALANCE = 0x02;
	public final static byte READ_RFCARD_CARDNO = 0x03;
	public final static byte QUIK_PAY = 0x04;
	public final static byte BALANCE_QUERY = 0x05;
	public final static byte CONSUME = 0x06;
	public final static byte NONENAMED_ACCOUNDLOAD_ICCARD = 0x07;
	public final static byte KERNEL_RESET = 0x08;
	public final static byte END_PBOC = 0x09;
	public final static byte READ_CARD_TRANS_LOG = 0x0A;
	public final static byte READ_CARD_LOAD_LOG = 0x0B;
	private final static byte READ_CARD_LOAD_LOG_ALL = 0x0C;

	public final static byte ICCARD = 0x00;
	public final static byte RFCARD = 0x01;

	private AidlEMVL2 pboc2; // 新版PBOC接口
	private CheckBox cbIsTransTypeSimpleFlow;
	private CheckBox cbIsConfirmCardNo;
	private Spinner spKernelTye;
	private Spinner spICSlotChoose;

	/** 测试AID **/
	private static final String[] TEST_AID = new String[] {
			"9F0607A0000000031010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100",
			"9F0607A0000000032010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100",
			"9F0607A0000000033010DF0101009F08020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100",
			"9F0607A0000000041010DF0101009F08020002DF1105FC5080A000DF1205F85080F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100",
			"9F0607A0000000043060DF0101009F08020002DF1105FC5058A000DF1205F85058F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101",
			"9F0607A0000000651010DF0101009F08020200DF1105FC6024A800DF1205FC60ACF800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100",
			"9F0608A000000333010101DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000000100000DF2106000000100000",
			"9F0608A000000333010102DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000000100000DF2106000000100000",
			"9F0608A000000333010103DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000000100000DF2106000000100000",
			"9F0608A000000333010106DF0101009F08020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000000100000DF2106000000100000" };

	/** 测试公钥 **/
	private static final String[] TEST_CAPK = new String[] {
			"9F0605A0000000659F220109DF05083230303931323331DF060101DF070101DF028180B72A8FEF5B27F2B550398FDCC256F714BAD497FF56094B7408328CB626AA6F0E6A9DF8388EB9887BC930170BCC1213E90FC070D52C8DCD0FF9E10FAD36801FE93FC998A721705091F18BC7C98241CADC15A2B9DA7FB963142C0AB640D5D0135E77EBAE95AF1B4FEFADCF9C012366BDDA0455C1564A68810D7127676D493890BDDF040103DF03144410C6D51C2F83ADFD92528FA6E38A32DF048D0A",
			"9F0605A0000000659F220110DF05083230313231323331DF060101DF070101DF02819099B63464EE0B4957E4FD23BF923D12B61469B8FFF8814346B2ED6A780F8988EA9CF0433BC1E655F05EFA66D0C98098F25B659D7A25B8478A36E489760D071F54CDF7416948ED733D816349DA2AADDA227EE45936203CBF628CD033AABA5E5A6E4AE37FBACB4611B4113ED427529C636F6C3304F8ABDD6D9AD660516AE87F7F2DDF1D2FA44C164727E56BBC9BA23C0285DF040103DF0314C75E5210CBE6E8F0594A0F1911B07418CADB5BAB",
			"9F0605A0000000659F220112DF05083230313431323331DF060101DF070101DF0281B0ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681DF040103DF0314874B379B7F607DC1CAF87A19E400B6A9E25163E8",
			"9F0605A0000000659F220114DF05083230313631323331DF060101DF070101DF0281F8AEED55B9EE00E1ECEB045F61D2DA9A66AB637B43FB5CDBDB22A2FBB25BE061E937E38244EE5132F530144A3F268907D8FD648863F5A96FED7E42089E93457ADC0E1BC89C58A0DB72675FBC47FEE9FF33C16ADE6D341936B06B6A6F5EF6F66A4EDD981DF75DA8399C3053F430ECA342437C23AF423A211AC9F58EAF09B0F837DE9D86C7109DB1646561AA5AF0289AF5514AC64BC2D9D36A179BB8A7971E2BFA03A9E4B847FD3D63524D43A0E8003547B94A8A75E519DF3177D0A60BC0B4BAB1EA59A2CBB4D2D62354E926E9C7D3BE4181E81BA60F8285A896D17DA8C3242481B6C405769A39D547C74ED9FF95A70A796046B5EFF36682DC29DF040103DF0314C0D15F6CD957E491DB56DCDD1CA87A03EBE06B7B",
			"9F0605A0000003339F220101DF05083230303931323331DF060101DF070101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF040103DF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0",
			"9F0605A0000003339F220102DF05083230313431323331DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060",
			"9F0605A0000003339F220103DF05083230313731323331DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26",
			"9F0605A0000003339F220104DF05083230313731323331DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5",
			"9F0605A0000000039F220101DF05083230303931323331DF060101DF070101DF028180C696034213D7D8546984579D1D0F0EA519CFF8DEFFC429354CF3A871A6F7183F1228DA5C7470C055387100CB935A712C4E2864DF5D64BA93FE7E63E71F25B1E5F5298575EBE1C63AA617706917911DC2A75AC28B251C7EF40F2365912490B939BCA2124A30A28F54402C34AECA331AB67E1E79B285DD5771B5D9FF79EA630B75DF040103DF0314D34A6A776011C7E7CE3AEC5F03AD2F8CFC5503CC",
			"9F0605A0000000039F220107DF05083230313231323331DF060101DF070101DF028190A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725FDF040103DF0314B4BC56CC4E88324932CBC643D6898F6FE593B172",
			"9F0605A0000000049F220103DF05083230303931323331DF060101DF070101DF028180C2490747FE17EB0584C88D47B1602704150ADC88C5B998BD59CE043EDEBF0FFEE3093AC7956AD3B6AD4554C6DE19A178D6DA295BE15D5220645E3C8131666FA4BE5B84FE131EA44B039307638B9E74A8C42564F892A64DF1CB15712B736E3374F1BBB6819371602D8970E97B900793C7C2A89A4A1649A59BE680574DD0B60145DF040103DF03145ADDF21D09278661141179CBEFF272EA384B13BB",
			"9F0605A0000000039F220108DF05083230313431323331DF060101DF070101DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0BDF040103DF031420D213126955DE205ADC2FD2822BD22DE21CF9A8",
			"9F0605A0000000049F220104DF05083230313231323331DF060101DF070101DF028190A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5DF040103DF0314381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C",
			"9F0605A0000000039F220109DF05083230313631323331DF060101DF070101DF0281F89D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41DF040103DF03141FF80A40173F52D7D27E0F26A146A1C8CCB29046",
			"9F0605A0000000049F220105DF05083230313431323331DF060101DF070101DF0281B0B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597DF040103DF0314EBFA0D5D06D8CE702DA3EAE890701D45E274C845",
			"9F0605A0000000049F220106DF05083230313631323331DF060101DF070101DF0281F8CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747FDF040103DF0314F910A1504D5FFB793D94F3B500765E1ABCAD72D9",
			"9F0605A0000003339F220108DF050420301231DF060101DF070101DF028190B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BFDF040103DF0314EE23B616C95C02652AD18860E48787C079E8E85A",
			"9F0605A0000003339F220109DF05083230333031323331DF060101DF070101DF0281B0EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5DF040103DF0314A075306EAB0045BAF72CDD33B3B678779DE1F527",
			"9F0605A0000003339F22010ADF05083230333031323331DF060101DF070101DF028180B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EFDF040103DF0314C88BE6B2417C4F941C9371EA35A377158767E4E3",
			"9F0605A0000003339F22010BDF05083230333031323331DF060101DF070101DF0281F8CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157DF040103DF0314BD331F9996A490B33C13441066A09AD3FEB5F66C",
			"9F0605A0000003339F220180DF05083230333031323331DF060101DF070101DF028180CCDBA686E2EFB84CE2EA01209EEB53BEF21AB6D353274FF8391D7035D76E2156CAEDD07510E07DAFCACABB7CCB0950BA2F0A3CEC313C52EE6CD09EF00401A3D6CC5F68CA5FCD0AC6132141FAFD1CFA36A2692D02DDC27EDA4CD5BEA6FF21913B513CE78BF33E6877AA5B605BC69A534F3777CBED6376BA649C72516A7E16AF85DF0403010001DF0314A5E44BB0E1FA4F96A11709186670D0835057D35E",
			"9F0605A0000000049F2201F6DF05083230303931323331DF060101DF070101DF0281E0A25A6BD783A5EF6B8FB6F83055C260F5F99EA16678F3B9053E0F6498E82C3F5D1E8C38F13588017E2B12B3D8FF6F50167F46442910729E9E4D1B3739E5067C0AC7A1F4487E35F675BC16E233315165CB142BFDB25E301A632A54A3371EBAB6572DEEBAF370F337F057EE73B4AE46D1A8BC4DA853EC3CC12C8CBC2DA18322D68530C70B22BDAC351DD36068AE321E11ABF264F4D3569BB71214545005558DE26083C735DB776368172FE8C2F5C85E8B5B890CC682911D2DE71FA626B8817FCCC08922B703869F3BAEAC1459D77CD85376BC36182F4238314D6C4212FBDD7F23D3DF040103DF0314502909ED545E3C8DBD00EA582D0617FEE9F6F684",
			"9F0605A0000003339F220185DF05083230323431323331DF060101DF070101DF0281f8C9242EC6030F10E5225E722AA17D9DC894299233AEC3219B950D4F243AF530FA13E3A31AFAA0D4BF4DE562B6B4C3108AEBBC6CB080F90770D532F241BC1536401E1BF72F9DC1B08933B9BF77403F6A0FB5777BAA4C9BE91574BBBFB521342A20386790512221F477FBC53FF1B6533A015815435410EC272F0A34EA0735C439677D7E46FBA766EC00CED59B6715E3412D6FB8A934BF9D1497A24A6252C52D7586FD66A450FB5D2B4484EC923061439622BC0535316CD4231C13C627BF4D2EDE1C02C802464658F1B9D7FF23A3698510FA90D0C3164942FB359255CD823CB2635B3F167FBDFC900641B970D602A2771A7F4F94DF6D34BE8BBBDF040103DF031410AC0A99C88419D84BF45A0B97E7B7470E01C4F1" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.pboc_activity2);
		super.onCreate(savedInstanceState);

		cbIsTransTypeSimpleFlow = (CheckBox) findViewById(R.id.isTransTypeSimpleFlow);
		cbIsConfirmCardNo = (CheckBox) findViewById(R.id.isConfirmCardNo);
		spKernelTye = (Spinner) findViewById(R.id.spKernelType);
		spICSlotChoose = (Spinner) findViewById(R.id.ic_slot_choose);
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			spICSlotChoose.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			pboc2 = AidlEMVL2.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PBOC2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setKenrelType(View v) {
		showMessage(getString(R.string.pboc_set_kernel_type));
		try {
			boolean res = pboc2.setParameters(
					EMVConstant.ParamType.SETKERNEL_TYPE,
					Byte.parseByte((String) spKernelTye.getSelectedItem()));

			if (res) {
				showMessage(getString(R.string.pboc_set_kernel_type_success));
			} else {
				showMessage(getString(R.string.pboc_set_kernel_type_failed));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_set_kernel_type_exception));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_set_kernel_type_exception));
		}
	}

	/**
	 * 检卡
	 */
	public void searchCard(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(SERACHE_CARD, false, false, true,
					getString(R.string.pboc_search_need_card), null);
		} else {
			allOperateStart(SERACHE_CARD, true, true, true,
					getString(R.string.pboc_search_need_card), null);
		}
	}

	/**
	 * 取消检卡
	 */
	public void cancelSearchCard(View v) {
		try {
			pboc2.cancelCheckCard();
			showMessage(getString(R.string.pboc_cancel_search_success));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_cancel_search_failed),
					Color.RED);
		}
	}

	/**
	 * 读取卡号
	 */
	public void readcardno(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(READ_CARDNO, false, false, true,
					getString(R.string.pboc_getcno_need_card),
					getString(R.string.pboc_read_cno));
		} else {
			allOperateStart(READ_CARDNO, true, true, true,
					getString(R.string.pboc_getcno_need_card),
					getString(R.string.pboc_read_cno));
		}
	}

	/**
	 * 读取脱机余额（接触式）
	 */
	public void readCardOfflineBalance(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			showMessage(getString(R.string.q60_ic_unsupport));
		} else {
		allOperateStart(READ_OFFILINE_BALANCE, false, true, false,
				getString(R.string.pboc_offline_balance_nedd_ic),
				getString(R.string.pboc_offline_balance_ic));
		}
	}

	/**
	 * 读取脱机余额（非接触式）
	 */
	public void readRfCardOfflineBalance(View v) {
		allOperateStart(READ_OFFILINE_BALANCE, false, false, true,
				getString(R.string.pboc_offline_balance_nedd_rf),
				getString(R.string.pboc_offline_balance_rf));

	}

	/**
	 * 快速支付
	 */
	public void quickPay(View v) {
		allOperateStart(QUIK_PAY, false, false, true,
				getString(R.string.pboc_quickpay_need_rf),
				getString(R.string.pboc_quick_pay));
	}

	/**
	 * 余额查询
	 */
	public void queryCardBalance(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(BALANCE_QUERY, false, false, true,
					getString(R.string.pboc_query_balance_need_ic),
					getString(R.string.pboc_query_balance));
		} else {
			allOperateStart(BALANCE_QUERY, true, true, true,
					getString(R.string.pboc_query_balance_need_ic),
					getString(R.string.pboc_query_balance));
		}

	}

	/**
	 * 消费
	 */
	public void consume(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(CONSUME, false, false, true,
					getString(R.string.pboc_consume_need_ic),
					getString(R.string.pboc_consume));
		} else {
			allOperateStart(CONSUME, true, true, true,
					getString(R.string.pboc_consume_need_ic),
					getString(R.string.pboc_consume));
		}

	}

	/**
	 * 非指定账户圈存（接触式）
	 */
	public void noneNamedAccountLoad(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(NONENAMED_ACCOUNDLOAD_ICCARD, false, false, true,
					getString(R.string.pboc_nonename_accountload),
					getString(R.string.pboc_nonename_accountload_ic));
		} else {
			allOperateStart(NONENAMED_ACCOUNDLOAD_ICCARD, true, true, true,
					getString(R.string.pboc_nonename_accountload),
					getString(R.string.pboc_nonename_accountload_ic));
		}
	}

	/**
	 * 内核复位
	 */
	public void reset(View v) {
		try {
			showMessage(getString(R.string.pboc_reset));
			pboc2.endPBOC();
			showMessage(getString(R.string.pboc_reset_succes));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_reset_exception), Color.RED);
		}
	}

	/**
	 * 终止PBOC流程
	 */
	public void abortProcess(View v) {
		try {
			showMessage(getString(R.string.pboc_abort));
			pboc2.abortPBOC();
			showMessage(getString(R.string.pboc_abort_success));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_abort_exception), Color.RED);
		}
	}

	/**
	 * 读取卡片交易日志
	 */
	public void readCardTransLog(View v) {

		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			showMessage(getString(R.string.q60_ic_unsupport));
		} else {
			allOperateStart(READ_CARD_TRANS_LOG, false, true, false,
					getString(R.string.pboc_read_translog_ic),
					getString(R.string.pboc_translog_ic));
		}
	}

	/**
	 * 读取非接触式卡片交易数据
	 */
	public void readRfCardTransLog(View v) {
		allOperateStart(READ_CARD_TRANS_LOG, false, false, true,
				getString(R.string.pboc_read_translog_rf),
				getString(R.string.pboc_translog_rf));
	}

	public void readCardLoadLog(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(READ_CARD_LOAD_LOG, false, false, true,
					getString(R.string.pboc_read_loadlog_one),
					getString(R.string.pboc_loadlog_one));
		} else {
			allOperateStart(READ_CARD_LOAD_LOG, false, true, true,
					getString(R.string.pboc_read_loadlog_one),
					getString(R.string.pboc_loadlog_one));
		}
	}

	public void readCardLoadLogAll(View v) {
		if (GetVersionCode.getTerminalModel().trim().equals("Q60")) {
			allOperateStart(READ_CARD_LOAD_LOG_ALL, false, false, true,
					getString(R.string.pboc_read_loadlog_all),
					getString(R.string.pboc_loadlog_all));
		} else {
			allOperateStart(READ_CARD_LOAD_LOG_ALL, false, true, true,
					getString(R.string.pboc_read_loadlog_all),
					getString(R.string.pboc_loadlog_all));
		}

	}

	/**
	 * 清空内核IC卡交易日志
	 */
	public void clearKernelICTransLog(View v) {
		showMessage(getString(R.string.pboc_clear_kernel_translog));
		try {
			boolean b = pboc2.clearKernelICTransLog();
			if (b) {
				showMessage(getString(R.string.pboc_clear_kernel_translog_success));
			} else {
				showMessage(getString(R.string.pboc_clear_kernel_translog_failed));
			}
		} catch (Exception e) {
			showMessage(
					getString(R.string.pboc_clear_kernel_translog_exception),
					Color.RED);
			e.printStackTrace();
		}
	}

	/**
	 * 清空AID参数
	 */
	public void clearAllAID(View v) {
		showMessage(getString(R.string.pboc_clear_aid));
		try {
			boolean res = pboc2.updateAID(
					EMVConstant.AidCapkOptFlag.AID_CAPK_OPT_REMOVEALL_FLAG,
					null);
			if (!res) {
				showMessage(getString(R.string.pboc_clear_aid_failed));
				return;
			}
			showMessage(getString(R.string.pboc_clear_aid_success));
		} catch (Exception e1) {
			e1.printStackTrace();
			showMessage(getString(R.string.pboc_clear_aid_exception), Color.RED);
			return;
		}
	}

	/**
	 * 导入一组AID参数
	 */
	public void addAID(View v) {
		showMessage(getString(R.string.pboc_add_aid));
		try {
			for (String aid : TEST_AID) {
				boolean res = pboc2
						.updateAID(
								EMVConstant.AidCapkOptFlag.AID_CAPK_OPT_ADDORUPDATE_FLAG,
								aid);
				if (!res) {
					showMessage(getString(R.string.pboc_updateaid_failed));
					return;
				}
			}
			showMessage(getString(R.string.pboc_updateaid_success));
		} catch (Exception e1) {
			e1.printStackTrace();
			showMessage(getString(R.string.pboc_add_aid_exception), Color.RED);
			return;
		}
	}

	/**
	 * 删除一组AID参数
	 */
	public void deleteAID(View v) {
		showMessage(getString(R.string.pboc_delete_now_aid) + TEST_AID[0]);
		try {
			// 自测使用
			boolean res = pboc2.updateAID(
					EMVConstant.AidCapkOptFlag.AID_CAPK_OPT_REMOVE_FLAG,
					TEST_AID[0]);
			if (!res) {
				showMessage(getString(R.string.pboc_delete_aid_failed));
				return;
			}
			showMessage(getString(R.string.pboc_delete_aid_success));
		} catch (Exception e1) {
			e1.printStackTrace();
			showMessage(getString(R.string.pboc_delete_aid_exception),
					Color.RED);
			return;
		}
	}

	/**
	 * 清空CA参数
	 */
	public void clearAllCA(View v) {
		showMessage(getString(R.string.pboc_clear_ca));
		try {
			// 自测使用
			boolean res = pboc2.updateCAPK(
					EMVConstant.AidCapkOptFlag.AID_CAPK_OPT_REMOVEALL_FLAG,
					null);
			if (!res) {
				showMessage(getString(R.string.pboc_clear_ca_failed));
				return;
			}
			showMessage(getString(R.string.pboc_clear_ca_success));
		} catch (Exception e1) {
			e1.printStackTrace();
			showMessage(getString(R.string.pboc_clear_ca_exception), Color.RED);
			return;
		}
	}

	/**
	 * 导入一组AID参数
	 */
	public void addCA(View v) {
		showMessage(getString(R.string.pboc_add_ca));
		try {
			// 自测使用
			for (String aid : TEST_CAPK) {
				boolean res = pboc2
						.updateCAPK(
								EMVConstant.AidCapkOptFlag.AID_CAPK_OPT_ADDORUPDATE_FLAG,
								aid);
				if (!res) {
					showMessage(getString(R.string.pboc_add_ca_failed));
					return;
				}
			}
			showMessage(getString(R.string.pboc_add_ca_success));
		} catch (Exception e1) {
			e1.printStackTrace();
			showMessage(getString(R.string.pboc_add_ca_exception), Color.RED);
			return;
		}
	}

	/**
	 * 删除一组CA参数
	 */
	public void deleteCA(View v) {
		showMessage(getString(R.string.pboc_delete_now_ca) + TEST_CAPK[1]);
		try {
			boolean res = pboc2.updateCAPK(
					EMVConstant.AidCapkOptFlag.AID_CAPK_OPT_REMOVE_FLAG,
					TEST_CAPK[1]);
			if (!res) {
				showMessage(getString(R.string.pboc_delete_ca_failed));
				return;
			}
			showMessage(getString(R.string.pboc_delete_ca_success));
		} catch (Exception e1) {
			e1.printStackTrace();
			showMessage(getString(R.string.pboc_delete_ca_exception), Color.RED);
			return;
		}
	}

	// AID参数读取
	public void aidParamRead(View v) {
		try {
			byte[] res1 = pboc2.aidParamRead(new byte[] { 0x01 });
			Log.e("pboc aid", "len :" + res1[0]);

			if (res1[0] > 0) {
				for (int i = 0; i < res1[0]; i++) {
					byte[] res = pboc2
							.aidParamRead(new byte[] { 0x02, (byte) i });
					Log.e("pboc aid",
							"index :" + i + "\ndata:" + HexUtil.bcd2str(res));

					if (null != res)
						showMessage(getString(R.string.pboc_read_aid_0)
								+ HexUtil.bcd2str(res));
					else
						showMessage(getString(R.string.pboc_read_aid_0)
								+ "null");
				}
			} else {
				showMessage(getString(R.string.pboc_read_aid_0) + "null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// CA公钥参数读取
	public void caPublicKeyParamRead(View v) {
		try {
			byte[] res = pboc2.caPublicKeyParamRead(new byte[] { 0x02, 0x01 });
			if (null != res)
				showMessage(getString(R.string.pboc_read_ca_1)
						+ HexUtil.bcd2str(res));
			else
				showMessage(getString(R.string.pboc_read_ca_1) + "null");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 所有操作的入口（检卡） 除了少数单独的接口，交易的接口调用顺序为检卡-ProccessPboc()
	 * 传入不同的参数进行不同的操作，在回调中对各个返回进行相应的处理，完成各个流程
	 * 
	 * @param operateId
	 * @param msgPrompt
	 * @param msg
	 */
	public void allOperateStart(final byte operateId, boolean isCheckMag,
			boolean isCheckIC, boolean isChecRF, final String msgPrompt,
			final String msg) {
		showMessage(msgPrompt);
		try {
			pboc2.checkCard(isCheckMag, isCheckIC, isChecRF, 60000,
					new AidlCheckCardListener.Stub() {

						@Override
						public void onCanceled() throws RemoteException {
							showMessage(getString(R.string.pboc_cancel_search));
						}

						@Override
						public void onError(int arg0) throws RemoteException {
							showMessage(getString(R.string.pboc_search_card_error)
									+ arg0);
						}

						@Override
						public void onFindICCard() throws RemoteException {
							showMessage(getString(R.string.pboc_find_ic));
							if (operateId != SERACHE_CARD) {
								allProcess(operateId, ICCARD, msg);
							}
						}

						@Override
						public void onFindMagCard(ParcelableTrackData arg0)
								throws RemoteException {
							switch (operateId) {
							case SERACHE_CARD:
								try {
									showMessage(getString(R.string.pboc_find_magcard));
									showMessage(getString(R.string.pboc_magcard_type)
											+ arg0.getCardType()
											+ "--0磁条卡,1带芯片的磁条卡");
									showMessage(getString(R.string.pboc_magcard_no)
											+ arg0.getCardNo());
									showMessage(getString(R.string.pboc_magcard_no)
											+ arg0.getMaskedCardNo());
									showMessage(getString(R.string.pboc_magcard_expiredate)
											+ arg0.getExpireDate());
									showMessage(getString(R.string.pboc_magcard_servicecode)
											+ arg0.getServiceCode());
									showMessage(getString(R.string.pboc_magcard_first)
											+ ((arg0.getWholeFirstTrackData() != null) ? (new String(
													arg0.getFirstTrackData()))
													: ""));
									showMessage(getString(R.string.pboc_magcard_second)
											+ new String(arg0
													.getSecondTrackData()));
									showMessage(getString(R.string.pboc_magcard_third)
											+ ((arg0.getWholeThirdTrackData() != null) ? (new String(
													arg0.getThirdTrackData()))
													: ""));
									showMessage(getString(R.string.pboc_magcard_encrydata)
											+ ((arg0.getTrackDataEncrypeData() != null) ? (HexUtil.bcd2str(arg0
													.getTrackDataEncrypeData()))
													: ""));
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
							case READ_CARDNO:
								showMessage(getString(R.string.pboc_magcard_no)
										+ arg0.getCardNo());
								break;
							case CONSUME:
							case BALANCE_QUERY:
							case NONENAMED_ACCOUNDLOAD_ICCARD:
								showMessage(getString(R.string.pboc_nomag_onlyic));
								break;
							default:
								break;
							}
						}

						@Override
						public void onFindRFCard() throws RemoteException {
							showMessage(getString(R.string.pboc_find_rf));
							switch (operateId) {
//							case CONSUME:
//							case BALANCE_QUERY:
							case NONENAMED_ACCOUNDLOAD_ICCARD:
								showMessage(getString(R.string.pboc_norf_onlyic));
								break;
							case SERACHE_CARD:
								break;
							default:
								allProcess(operateId, RFCARD, msg);
								break;
							}
						}

						@Override
						public void onSwipeCardFail() throws RemoteException {
							showMessage(getString(R.string.pboc_swipe_failed));
						}

						@Override
						public void onTimeout() throws RemoteException {
							showMessage(getString(R.string.pboc_search_card_timeout));
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_search_exception), Color.RED);
		}
	}

	/**
	 * ProceePboc
	 * 
	 * @param operateId
	 * @param cardType
	 * @param msg
	 */
	public void allProcess(final byte operateId, final byte cardType,
			final String msg) {
		EmvTransData paramEmvTransData = new EmvTransData();
		if (cardType == ICCARD) {
			paramEmvTransData
					.setSlotType((byte) EMVConstant.SlotType.SLOT_TYPE_IC); // 界面类型

			// 判断是否选择简易流程，仅IC卡有效果
			if (cbIsTransTypeSimpleFlow.isChecked()) {
				paramEmvTransData.setTransTypeSimpleFlow(true);
			}
			// 判断是否更快速显示卡号，仅IC卡有效果（用于能快速的显示卡号）
			if (cbIsConfirmCardNo.isChecked()) {
				paramEmvTransData.setConfirmCardNo(true);
			}
		} else {
			paramEmvTransData
					.setSlotType((byte) EMVConstant.SlotType.SLOT_TYPE_RF); // 界面类型
		}

		switch (operateId) {
		case READ_CARDNO:
			// 读取卡号
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.TRANS_TYPE_READCARDNO); // 交易类型
			break;
		case READ_OFFILINE_BALANCE:
			// 脱机余额查询
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.CARD_BALANCE_INQUIRY); // 交易类型
			break;
		case READ_CARD_TRANS_LOG:
			// 读取交易日志
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.CARD_TRANSACTION_LOG_QUERY);
			break;
		case QUIK_PAY:
		case CONSUME:
			// 消费
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.TRANS_TYPE_CONSUME); // 交易类型
			break;
		case BALANCE_QUERY: // 余额查询（联机）
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.TRANS_TYPE_BALANCE_QUERY);
			break;
		case NONENAMED_ACCOUNDLOAD_ICCARD:
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.TRANS_TYPE_NON_NAMED_ACCOUNTA_CREDIT_FOR_LOAD);
			break;
		case READ_CARD_LOAD_LOG:
		case READ_CARD_LOAD_LOG_ALL:
			// 读取圈存日志
			paramEmvTransData
					.setTranstype((byte) EMVConstant.TransType.CARD_TRANSFER_LOG_QUERY);

			if (operateId == READ_CARD_LOAD_LOG_ALL) {
				paramEmvTransData.setResv(new byte[] { 0x01, 0x00, 0x00 });
			}
			break;
		default:
			break;
		}

		// 请求输入金额位置,可配置，除了非接消费流程必须选择BEFORE_DISPLAY_CARD_NUMBER其他两种都可以
		paramEmvTransData
				.setRequestAmtPosition((byte) EMVConstant.AmtPosition.BEFORE_DISPLAY_CARD_NUMBER);

		if (operateId == QUIK_PAY) {
			// 走脱机交易必须设置电子现金以及不强制联机
			paramEmvTransData.setIsEcashEnable(true); // 是否支持电子现金
			paramEmvTransData.setIsForceOnline(false); // 是否强制联机
			Log.e("pboc", "setOnlyOffline:" + true);
			paramEmvTransData.setOnlyOffline(true);

			// 以上设置如果电子现金脱机交易失败，则会走联机交易，如果希望脱机交易失败返回错误不走联机交易，则设置
			// paramEmvTransData.setResv(new byte[]);仅脱机（脱机失败也不联机）
		} else if (operateId == NONENAMED_ACCOUNDLOAD_ICCARD) {
			paramEmvTransData.setIsEcashEnable(true); // 是否支持电子现金
			paramEmvTransData.setIsForceOnline(true); // 是否强制联机
		} else {
			paramEmvTransData.setIsEcashEnable(false); // 是否支持电子现金
			paramEmvTransData.setIsForceOnline(true); // 是否强制联机
		}

		paramEmvTransData.setIsSmEnable(false); // 是否支持国密算法
		paramEmvTransData
				.setEMVFlow((byte) EMVConstant.EMVFlowSelect.EMV_FLOW_PBOC); // 流程选择

		try {
			pboc2.processPBOC(paramEmvTransData, new PBOCListener.Stub() {

				@Override
				public void onConfirmCardInfo(CardInfoData arg0)
						throws RemoteException {
					showMessage(getString(R.string.pboc_back_cno)
							+ arg0.getCardno());

					// 导入卡信息确认结果
					if (operateId != READ_CARDNO) {
						pboc2.importConfirmCardInfoRes(true);
					}
				}

				@Override
				public void onError(int arg0) throws RemoteException {
					showMessage(getString(R.string.pboc_error) + "，"
							+ getString(R.string.error_code) + arg0);
				}

				@Override
				public void onReadCardLoadLog(String arg0, String arg1,
						CardLoadLog[] arg2) throws RemoteException {
					showMessage(getString(R.string.pboc_read_loadlog_success)
							+ "," + "atc：" + arg0 + "  logcheckCode :" + arg1);
					for (int i = 0; i < arg2.length; i++) {
						showMessage(getString(R.string.pboc_loadlog_index) + i);
						showMessage(getString(R.string.pboc_loadlog_transdate)
								+ arg2[i].getTransDate());
						showMessage(getString(R.string.pboc_loadlog_transtime)
								+ arg2[i].getTransTime());
					}
				}

				@Override
				public void onReadCardOffLineBalance(String firstMoneyCode,
						String firstBalance, String secondMoneyCode,
						String secondBalance) throws RemoteException {
					showMessage(getString(R.string.pboc_read_offline_balance_result)
							+ firstBalance);
					showMessage(getString(R.string.pboc_read_offline_balance_end));
				}

				@Override
				public void onReadCardTransLog(CardTransLog[] arg0)
						throws RemoteException {
					// 卡片交易日志
					if (arg0 != null) {
						showMessage(getString(R.string.pboc_translog_success_sum)
								+ arg0.length);
						for (CardTransLog log : arg0) {
							showMessage(log.toString());
						}
					} else
						showMessage(getString(R.string.pboc_translog_failed_null));
				}

				@Override
				public void onRequestOnline() throws RemoteException {
					showMessage(getString(R.string.pboc_request_need_online));
					// 读取5F34卡序列号的时候不需要在请求联机这个时候读取，
					// 但是9F26需要在请求联机交易时读取
					readKernelData();
					showMessage(getString(R.string.pboc_import_online_result)
							+ ",[8A023030]");

					// 判断返回报文中的交易结果。
					// 按照如下导入联机结果：
					// OnlineResult+[ iccresponse + ackdata ]
					// 【OnlineResult】：联机结果，1byte
					// 00：联机失败；
					// 01：联机成功；
					// 【iccresponse】：后台应答码，TLV格式，如8A023030，其中V为ASC码格式，2bytes，
					// 8A02固定，V取值如下：
					// “00”：联机批准；
					// “01”：发卡行语音参考；
					// “05”：联机拒绝；
					// 【ackdata】:后台应答的55域数据，nbytes。如果解析出来有就加在30318A023030后面，没有就不加
					pboc2.importOnlineResp(true, "00", null); // 联机结果为批准
				}

				@Override
				public void onTransResult(byte arg0) throws RemoteException {
					switch (arg0) {
					case EMVConstant.TransResult.TRANS_RESULT_ABORT:
						showMessage(getString(R.string.pboc_trans_abort));
						break;
					case EMVConstant.TransResult.TRANS_RESULT_APPROVE:
						showMessage(getString(R.string.pboc_trans_accept));

						if (operateId == READ_CARDNO && cardType == RFCARD) {
							showMessage(getString(R.string.pboc_rf_cno)
									+ readCardNo());
						} else {
							showMessage(msg + getString(R.string.success));
						}

						break;
					case EMVConstant.TransResult.TRANS_RESULT_FALLBACK:
						showMessage(getString(R.string.pboc_trans_fallback));
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Dialog dialog = createSingleConfirmDialog(
										getString(R.string.pboc_trans_fallback),
										new DialogBtnListener() {
											@Override
											public void onConfirm() {
											}

											@Override
											public void onCancel() {
											}
										});
								dialog.show();
							}
						});

						break;
					case EMVConstant.TransResult.TRANS_RESULT_OTHER:
						showMessage(getString(R.string.pboc_trans_other));
						break;
					case EMVConstant.TransResult.TRANS_RESULT_OTHERINTERFACES:
						showMessage(getString(R.string.pboc_trans_other_interface));
						break;
					case EMVConstant.TransResult.TRANS_RESULT_REFUSE:
						showMessage(msg + getString(R.string.failed));
						break;
					default:
						showMessage(getString(R.string.pboc_trans_error) + "，"
								+ getString(R.string.error_code) + arg0);
					}

					pboc2.endPBOC();
				}

				@Override
				public void requestAidSelect(int arg0, String[] arg1)
						throws RemoteException {
					showMessage(getString(R.string.pboc_request_aidselect));
					for (String aid : arg1) {
						showMessage("[" + aid + "]");
					}

					boolean b = pboc2.importAidSelectRes(1);

					if (b) {
						showMessage(getString(R.string.pboc_request_adiselect_success));
					} else {
						showMessage(getString(R.string.pboc_request_adiselect_failed));
					}
				}

				@Override
				public void requestEcashTipsConfirm() throws RemoteException {
					showMessage(getString(R.string.pboc_request_elecash));
				}

				@Override
				public void requestImportAmount(int arg0)
						throws RemoteException {
					showMessage(getString(R.string.pboc_request_money));

					String money = 3.14 + "";
					if (operateId == QUIK_PAY) {
						money = "0.01";
					}
					boolean b = pboc2.importAmount(money);

					if (b) {
						showMessage(getString(R.string.pboc_import_money)
								+ money + getString(R.string.success));
					} else {
						showMessage(getString(R.string.pboc_import_money)
								+ money + getString(R.string.failed));
					}
				}

				@Override
				// 导入PIN
				public void requestImportPin(int arg0, boolean arg1, String arg2)
						throws RemoteException {
					showMessage(getString(R.string.pboc_input_pin));
					showMessage("arg0" + arg0 + "\narg1:" + arg1 + "\narg2:"
							+ arg2);

					showMessage(getString(R.string.pboc_importing_pin)
							+ ":26888888FFFFFFFF" + "");
					pboc2.importPin("26888888FFFFFFFF");
				}

				@Override
				// 请求提示信息
				public void requestTipsConfirm(String arg0)
						throws RemoteException {
					showMessage(getString(R.string.pboc_request_msg_excute));
					showMessage(arg0);

					pboc2.importMsgConfirmRes(true);
				}

				@Override
				public void requestUserAuth(int arg0, String arg1)
						throws RemoteException {
					showMessage(getString(R.string.pboc_request_user));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(msg + getString(R.string.exception), Color.RED);
		}
	}

	/**
	 * 读取内核数据
	 */
	public void readKernelData() {
		showMessage(getString(R.string.pboc_read_kerneldata));
		try {
			byte[] outputBuffer = new byte[1024];
			// 5F34 卡序列号
			int ret = pboc2.readKernelData(EMVTAGS.getF55Taglist(),
					outputBuffer);
			showMessage(getString(R.string.pboc_get_55tag_len) + " [ " + ret
					+ " ]" + getString(R.string.pboc_byte));
			if (ret > 0) {

				showMessage(getString(R.string.pboc_read_55tag_success));
				outputBuffer = Arrays.copyOfRange(outputBuffer, 0, ret);
				showMessage(getString(R.string.pboc_55tag_value)
						+ HexUtil.bcd2str(outputBuffer));
				outputBuffer = new byte[500];
			} else {
				showMessage(getString(R.string.pboc_read_55tag_failed));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.pboc_read_55tag_exception),
					Color.RED);
		}
	}

	private String readCardNo() {
		try {
			/* 此处代码用来读取非接卡卡号,当快速支付交易接受或者拒绝等，或者快速支付要求连接时均可读取到 */
			byte[] buffer = new byte[512];

			int res = pboc2.readKernelData(
					new byte[] { 0x57, 0x00, 0x00, 0x00 }, buffer);

			if (res >= 0) {
				TlvData data1 = new TlvData(HexUtil.bcd2str(buffer), true);

				String str = data1.getValueByTag("57");
				String account = str.substring(0, str.indexOf("D"));

				return account;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static interface DialogBtnListener {
		public void onConfirm();

		public void onCancel();
	}

	private Dialog createSingleConfirmDialog(String msg,
			final DialogBtnListener listener) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage(msg);
		dialog.setCancelable(false);

		dialog.setButton2(getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						listener.onConfirm();
					}
				});

		return dialog;
	}

	/**
	 * 2017-05-04 新增PBOC时卡槽选择接口 wangwenxun@centerm.com
	 * 
	 * @param view
	 */
	public void setSlotChoose(View view) {
		// 0x00 通用
		// 0x01 不允许立式IC卡
		// 0x02 仅允许立式IC卡
		if (spICSlotChoose.getSelectedItemPosition() < 3) {
			try {
				switch (spICSlotChoose.getSelectedItemPosition()) {
				case 0:
					if (pboc2.setICSlot((byte) 0x00)) {
						showMessage("Both IC Slot support!");
					}
					break;
				case 1:
					if (pboc2.setICSlot((byte) 0x01)) {
						showMessage("None Vertical IC Slot support");
					}
					break;
				case 2:
					if (pboc2.setICSlot((byte) 0x02)) {
						showMessage("Only vertical IC Slot support");
					}
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			showMessage("Wrong param chosed");
		}
	}
}
