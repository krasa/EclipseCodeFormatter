package importorder.example;

import importorder.example.root.AAAA;
import importorder.example.root.BFoo;
import importorder.example.root.DFoo;
import importorder.example.root.aaa;
import importorder.example.root.Aa.AFoo;
import importorder.example.root.Aaa.AaaFoo;
import importorder.example.root.Bb.bb;
import importorder.example.root.aB.Ab;
import importorder.example.root.ba.ba;
import importorder.example.root.bc.bc;

public class Main {
	public static void main(String[] args) {
		new AaaFoo();
		new AAAA();
		new aaa();
		new DFoo();
		new AFoo();
		new BFoo();
		new Ab();
		new bc();
		new bb();
		new ba();

	}
}
